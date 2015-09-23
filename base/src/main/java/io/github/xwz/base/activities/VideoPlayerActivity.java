package io.github.xwz.base.activities;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.github.xwz.base.R;
import io.github.xwz.base.Utils;
import io.github.xwz.base.api.EpisodeBaseModel;
import io.github.xwz.base.api.PlayHistory;
import io.github.xwz.base.content.ContentManagerBase;
import io.github.xwz.base.player.DurationLogger;
import io.github.xwz.base.player.EventLogger;
import io.github.xwz.base.player.HlsRendererBuilder;
import io.github.xwz.base.player.VideoPlayer;
import io.github.xwz.base.views.PlaybackControls;
import io.github.xwz.base.views.VideoPlayerView;

/**
 * An activity that plays media using {@link VideoPlayer}.
 */
public abstract class VideoPlayerActivity extends BaseActivity implements SurfaceHolder.Callback, VideoPlayer.Listener, VideoPlayer.CaptionListener,
        AudioCapabilitiesReceiver.Listener {

    private static final String TAG = "PlayerActivity";
    private static final String MEDIA_SESSION_TAG = "io.github.xwz.base.MEDIA_SESSION_TAG";
    private static final String RESUME_POSITION = "io.github.xwz.base.RESUME_POSITION";

    private static final CookieManager defaultCookieManager;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private EventLogger eventLogger;
    private MediaSession mediaSession;
    private PlaybackControls mediaController;
    private DurationLogger timeLogger;

    private VideoPlayerView videoPlayerView;

    private VideoPlayer player;
    private boolean playerNeedsPrepare;

    private long playerPosition;

    private Uri contentUri;

    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private AudioCapabilities audioCapabilities;

    private boolean ready = false;

    private EpisodeBaseModel mCurrentEpisode;
    private List<String> mOtherEpisodeUrls;
    private long resumePosition;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Action: " + action + ", tag: " + intent.getStringExtra(ContentManagerBase.CONTENT_TAG));
            if (ContentManagerBase.CONTENT_AUTH_DONE.equals(action)) {
                prepareStream(intent);
            }
            if (ContentManagerBase.CONTENT_AUTH_ERROR.equals(action)) {
                authFailed(intent);
            }
            if (ContentManagerBase.CONTENT_AUTH_FETCHING.equals(action)) {
                if (videoPlayerView != null) {
                    videoPlayerView.showStatusText("Preparing...");
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EpisodeBaseModel episode = (EpisodeBaseModel) getIntent().getSerializableExtra(ContentManagerBase.CONTENT_ID);
        mOtherEpisodeUrls = Arrays.asList(getIntent().getStringArrayExtra(ContentManagerBase.OTHER_EPISODES));
        resumePosition = getIntent().getLongExtra(RESUME_POSITION, 0);

        if (resumePosition <= 0 && episode.getResumePosition() > 0) {
            resumePosition = episode.getResumePosition();
            Log.d(TAG, "Resume from recently played");
        }

        setContentView(R.layout.video_player_activity);
        View root = findViewById(R.id.root);

        mediaController = new PlaybackControls(this);
        mediaController.setAnchorView(root);
        videoPlayerView = new VideoPlayerView(this, mediaController, root);

        audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(getApplicationContext(), this);

        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }

        playEpisode(episode);
    }

    private void playEpisode(EpisodeBaseModel episode) {
        releasePlayer();
        playerPosition = resumePosition;
        ready = false;
        mCurrentEpisode = episode;
        videoPlayerView.setEpisode(episode);
        getContentManger().fetchAuthToken(episode);
    }

    protected abstract ContentManagerBase getContentManger();

    private void prepareStream(Intent intent) {
        contentUri = getContentManger().getEpisodeStreamUrl(mCurrentEpisode);
        if (contentUri != null) {
            ready = true;
            Log.d(TAG, "Ready to play:" + mCurrentEpisode);
            preparePlayer();
        }
    }

    private void authFailed(Intent intent) {
        String href = intent.getStringExtra(ContentManagerBase.CONTENT_ID);
        String error = intent.getStringExtra(ContentManagerBase.CONTENT_TAG);
        Log.e(TAG, error + ":" + href);
        Utils.showToast(this, error);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
        videoPlayerView.configureSubtitleView();
        videoPlayerView.showShutter(false);

        // The player will be prepared on receiving audio capabilities.
        audioCapabilitiesReceiver.register();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!requestVisibleBehind(true)) {
            releasePlayer();
            audioCapabilitiesReceiver.unregister();
            videoPlayerView.showShutter(true);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    public void onVisibleBehindCanceled() {
        super.onVisibleBehindCanceled();
        releasePlayer();
        audioCapabilitiesReceiver.unregister();
        videoPlayerView.showShutter(true);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private void registerReceiver() {
        Log.i(TAG, "Register receiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ContentManagerBase.CONTENT_AUTH_FETCHING);
        filter.addAction(ContentManagerBase.CONTENT_AUTH_START);
        filter.addAction(ContentManagerBase.CONTENT_AUTH_DONE);
        filter.addAction(ContentManagerBase.CONTENT_AUTH_ERROR);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    // AudioCapabilitiesReceiver.Listener methods

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        boolean audioCapabilitiesChanged = !audioCapabilities.equals(this.audioCapabilities);
        if (player == null || audioCapabilitiesChanged) {
            this.audioCapabilities = audioCapabilities;
            releasePlayer();
            preparePlayer();
        } else if (player != null) {
            player.setBackgrounded(false);
        }
    }

    // Internal methods

    private VideoPlayer.RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(this, getString(R.string.app_name));
        return new HlsRendererBuilder(this, userAgent, contentUri.toString(), audioCapabilities);
    }

    private void preparePlayer() {
        if (ready) {
            if (player == null) {
                Log.d(TAG, "Prepare player, position:" + playerPosition);
                player = new VideoPlayer(getRendererBuilder());
                player.addListener(this);
                player.setCaptionListener(this);
                player.seekTo(playerPosition);
                playerNeedsPrepare = true;
                mediaController.setMediaPlayer(player.getPlayerControl());
                mediaController.setEnabled(true);
                mediaController.setPrevNextListeners(getNextEpisodeListener(), getPrevEpisodeListener());
                eventLogger = new EventLogger();
                eventLogger.startSession();
                player.addListener(eventLogger);
                player.setInfoListener(eventLogger);
                player.setInternalErrorListener(eventLogger);
                videoPlayerView.startDebugView(player);
                videoPlayerView.resetView();
                videoPlayerView.setMediaPlayer(player.getPlayerControl());

                timeLogger = new DurationLogger();
                timeLogger.addListener(30L, new DurationLogger.OnTimeReached() {
                    @Override
                    public void onPositionRemainingReached(long duration, long position) {
                        suggestNextEpisode();
                    }
                });
                timeLogger.addListener(0L, new DurationLogger.OnTimeReached() {
                    @Override
                    public void onPositionRemainingReached(long duration, long position) {
                        playNextEpisode();
                    }
                });
                player.addListener(timeLogger);
                timeLogger.start(player);
                if (mediaSession != null) {
                    mediaSession.release();
                }
                mediaSession = new MediaSession(this, MEDIA_SESSION_TAG);
                mediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
                mediaSession.setActive(true);
                updateMediaSessionData();

            }
            if (playerNeedsPrepare) {
                player.prepare();
                playerNeedsPrepare = false;
            }
            player.setSurface(videoPlayerView.getVideoSurface());
            player.setPlayWhenReady(true);
        }
    }

    private void releasePlayer() {
        if (player != null) {
            Log.d(TAG, "Release player");
            videoPlayerView.stopDebugView();
            playerPosition = player.getCurrentPosition();
            long duration = player.getDuration();
            player.getPlayerControl().pause();
            updatePlaybackState(ExoPlayer.STATE_IDLE);
            updateMediaSessionIntent();
            player.release();
            player = null;
            eventLogger.endSession();
            eventLogger = null;
            timeLogger.endSession();
            timeLogger = null;
            if (playerPosition >= duration) {
                mediaSession.setActive(false);
                mediaSession.release();
                mediaSession = null;
            }
        }
    }

    private void updateMediaSessionData() {
        if (mCurrentEpisode == null) {
            return;
        }
        final MediaMetadata.Builder builder = new MediaMetadata.Builder();

        updatePlaybackState(ExoPlayer.STATE_IDLE);

        updateMediaSessionIntent();

        builder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, mCurrentEpisode.getSeriesTitle());
        builder.putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, mCurrentEpisode.getTitle());
        builder.putLong(MediaMetadata.METADATA_KEY_DURATION, mCurrentEpisode.getDuration() * 1000);

        builder.putString(MediaMetadata.METADATA_KEY_TITLE, mCurrentEpisode.getSeriesTitle());
        builder.putString(MediaMetadata.METADATA_KEY_ARTIST, mCurrentEpisode.getTitle());

        Point size = new Point(getResources().getDimensionPixelSize(R.dimen.card_width),
                getResources().getDimensionPixelSize(R.dimen.card_height));

        Picasso.with(this).load(mCurrentEpisode.getThumbnail()).resize(size.x, size.y).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                builder.putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap);
                mediaSession.setMetadata(builder.build());
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
    }

    protected abstract Class getVideoPlayerActivityClass();

    private void updateMediaSessionIntent() {
        Intent intent = new Intent(this, getVideoPlayerActivityClass());
        intent.putExtra(ContentManagerBase.CONTENT_ID, mCurrentEpisode);
        String[] others = mOtherEpisodeUrls.toArray(new String[mOtherEpisodeUrls.size()]);
        intent.putExtra(ContentManagerBase.OTHER_EPISODES, others);
        intent.putExtra(RESUME_POSITION, playerPosition);

        PlayHistory.updateProgress(mCurrentEpisode, playerPosition);

        PendingIntent pending = PendingIntent.getActivity(this, 99, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mediaSession.setSessionActivity(pending);
    }

    private void updatePlaybackState(int playbackState) {
        PlaybackState.Builder state = new PlaybackState.Builder();
        long position = player.getCurrentPosition();
        if (ExoPlayer.STATE_PREPARING == playbackState) {
            state.setState(PlaybackState.STATE_CONNECTING, position, 1.0f);
        } else if (ExoPlayer.STATE_BUFFERING == playbackState) {
            state.setState(PlaybackState.STATE_BUFFERING, position, 1.0f);
        } else {
            if (player.getPlayerControl().isPlaying()) {
                state.setState(PlaybackState.STATE_PLAYING, position, 1.0f);
            } else {
                state.setState(PlaybackState.STATE_PAUSED, position, 1.0f);
            }
        }
        mediaSession.setPlaybackState(state.build());
    }

    private View.OnClickListener getNextEpisodeListener() {
        EpisodeBaseModel next = getNextEpisode(mCurrentEpisode);
        Log.d(TAG, "next episode:" + next);
        if (next != null) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playNextEpisode();
                }
            };
        }
        return null;
    }

    private View.OnClickListener getPrevEpisodeListener() {
        EpisodeBaseModel prev = getPrevEpisode(mCurrentEpisode);
        Log.d(TAG, "previous episode:" + prev);
        if (prev != null) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playPrevEpisode();
                }
            };
        }
        return null;
    }

    private EpisodeBaseModel getNextEpisode(EpisodeBaseModel current) {
        return getContentManger().findNextEpisode(mOtherEpisodeUrls, current.getHref());
    }

    private EpisodeBaseModel getPrevEpisode(EpisodeBaseModel current) {
        List<String> others = new ArrayList<>(mOtherEpisodeUrls);
        Collections.reverse(others);
        return getContentManger().findNextEpisode(others, current.getHref());
    }

    private void suggestNextEpisode() {
        EpisodeBaseModel next = getNextEpisode(mCurrentEpisode);
        Log.d(TAG, "Suggest next episode: " + next);
        if (next != null) {
            videoPlayerView.suggestNextEpisode(next);
        }
    }

    private void playNextEpisode() {
        EpisodeBaseModel next = getNextEpisode(mCurrentEpisode);
        Log.d(TAG, "Play next episode: " + next);
        if (next != null) {
            playEpisode(next);
        }
    }

    private void playPrevEpisode() {
        EpisodeBaseModel next = getPrevEpisode(mCurrentEpisode);
        Log.d(TAG, "Play previous episode: " + next);
        if (next != null) {
            playEpisode(next);
        }
    }

    // VideoPlayer.Listener implementation

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        videoPlayerView.onStateChanged(playWhenReady, playbackState);
        updatePlaybackState(playbackState);
    }

    @Override
    public void onError(Exception e) {
        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            int stringId = Util.SDK_INT < 18 ? R.string.drm_error_not_supported
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.drm_error_unsupported_scheme : R.string.drm_error_unknown;
            Toast.makeText(getApplicationContext(), stringId, Toast.LENGTH_LONG).show();
        }
        playerNeedsPrepare = true;
        videoPlayerView.showControls();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, float pixelWidthAspectRatio) {
        videoPlayerView.showShutter(false);
        videoPlayerView.setVideoFrameAspectRatio(height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
    }

    // VideoPlayer.CaptionListener implementation

    @Override
    public void onCues(List<Cue> cues) {
        videoPlayerView.setCues(cues);
    }

    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null) {
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null) {
            player.blockingClearSurface();
        }
    }
}