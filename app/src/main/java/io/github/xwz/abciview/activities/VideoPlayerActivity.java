package io.github.xwz.abciview.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.util.Util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Arrays;
import java.util.List;

import io.github.xwz.abciview.R;
import io.github.xwz.abciview.Utils;
import io.github.xwz.abciview.content.ContentManager;
import io.github.xwz.abciview.models.EpisodeModel;
import io.github.xwz.abciview.player.DurationLogger;
import io.github.xwz.abciview.player.EventLogger;
import io.github.xwz.abciview.player.HlsRendererBuilder;
import io.github.xwz.abciview.player.VideoPlayer;
import io.github.xwz.abciview.views.VideoPlayerView;

/**
 * An activity that plays media using {@link VideoPlayer}.
 */
public class VideoPlayerActivity extends BaseActivity implements SurfaceHolder.Callback, VideoPlayer.Listener, VideoPlayer.CaptionListener,
        AudioCapabilitiesReceiver.Listener {

    private static final String TAG = "PlayerActivity";

    private static final CookieManager defaultCookieManager;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private EventLogger eventLogger;
    private MediaController mediaController;
    private DurationLogger timeLogger;

    private VideoPlayerView videoPlayerView;

    private VideoPlayer player;
    private boolean playerNeedsPrepare;

    private long playerPosition;

    private Uri contentUri;

    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private AudioCapabilities audioCapabilities;

    private boolean ready = false;

    private EpisodeModel mCurrentEpisode;
    private List<String> mOtherEpisodeUrls;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Action: " + action + ", tag: " + intent.getStringExtra(ContentManager.CONTENT_TAG));
            if (ContentManager.CONTENT_AUTH_DONE.equals(action)) {
                prepareStream(intent);
            }
            if (ContentManager.CONTENT_AUTH_ERROR.equals(action)) {
                authFailed(intent);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EpisodeModel episode = (EpisodeModel) getIntent().getSerializableExtra(ContentManager.CONTENT_ID);
        mOtherEpisodeUrls = Arrays.asList(getIntent().getStringArrayExtra(ContentManager.OTHER_EPISODES));

        setContentView(R.layout.video_player_activity);
        View root = findViewById(R.id.root);

        mediaController = new MediaController(this);
        mediaController.setAnchorView(root);
        videoPlayerView = new VideoPlayerView(this, mediaController, root);

        audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(getApplicationContext(), this);

        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }

        playEpisode(episode);
    }

    protected void playEpisode(EpisodeModel episode) {
        releasePlayer();
        playerPosition = 0;
        ready = false;
        mCurrentEpisode = episode;
        videoPlayerView.setEpisode(episode);
        ContentManager.getInstance().fetchAuthToken(episode);
    }

    protected void prepareStream(Intent intent) {
        contentUri = ContentManager.getInstance().getEpisodeStreamUrl(mCurrentEpisode);
        if (contentUri != null) {
            ready = true;
            Log.d(TAG, "Ready to play:" + mCurrentEpisode);
            preparePlayer();
        }
    }

    protected void authFailed(Intent intent) {
        String href = intent.getStringExtra(ContentManager.CONTENT_ID);
        String error = intent.getStringExtra(ContentManager.CONTENT_TAG);
        Log.e(TAG, error + ":" + href);
        Utils.showToast(this, error);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
        videoPlayerView.configureSubtitleView();

        // The player will be prepared on receiving audio capabilities.
        audioCapabilitiesReceiver.register();
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
        audioCapabilitiesReceiver.unregister();
        videoPlayerView.showShutter(true);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private void registerReceiver() {
        Log.i(TAG, "Register receiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ContentManager.CONTENT_AUTH_START);
        filter.addAction(ContentManager.CONTENT_AUTH_DONE);
        filter.addAction(ContentManager.CONTENT_AUTH_ERROR);
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
        String userAgent = Util.getUserAgent(this, "ABC iview player");
        return new HlsRendererBuilder(this, userAgent, contentUri.toString(), audioCapabilities);
    }

    private void preparePlayer() {
        if (ready) {
            if (player == null) {
                player = new VideoPlayer(getRendererBuilder());
                player.addListener(this);
                player.setCaptionListener(this);
                player.seekTo(playerPosition);
                playerNeedsPrepare = true;
                mediaController.setMediaPlayer(player.getPlayerControl());
                mediaController.setEnabled(true);
                eventLogger = new EventLogger();
                eventLogger.startSession();
                player.addListener(eventLogger);
                player.setInfoListener(eventLogger);
                player.setInternalErrorListener(eventLogger);
                videoPlayerView.startDebugView(player);
                videoPlayerView.resetView();

                timeLogger = new DurationLogger();
                timeLogger.addListener(new Pair<Long, DurationLogger.OnTimeReached>(30L, new DurationLogger.OnTimeReached() {
                    @Override
                    public void onPositionRemainingReached(long duration, long position) {
                        suggestNextEpisode();
                    }
                }));
                timeLogger.addListener(new Pair<Long, DurationLogger.OnTimeReached>(0L, new DurationLogger.OnTimeReached() {
                    @Override
                    public void onPositionRemainingReached(long duration, long position) {
                        playNextEpisode();
                    }
                }));
                player.addListener(timeLogger);
                timeLogger.start(player);

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
            videoPlayerView.stopDebugView();
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
            eventLogger.endSession();
            eventLogger = null;
            timeLogger.endSession();
            timeLogger = null;
        }
    }

    protected EpisodeModel getNextEpisode(EpisodeModel current) {
        String next = null;
        boolean found = false;
        for (String href : mOtherEpisodeUrls) {
            if (found) {
                next = href;
                break;
            }
            found = href.equals(current.getHref());
        }
        if (!found && next == null && mOtherEpisodeUrls.size() > 0) {
            next = mOtherEpisodeUrls.get(0);
        }
        if (next != null) {
            return ContentManager.getInstance().getEpisode(next);
        }
        return null;
    }

    protected void suggestNextEpisode() {
        EpisodeModel next = getNextEpisode(mCurrentEpisode);
        Log.d(TAG, "Suggest next episode: " + next);
        if (next != null) {
            videoPlayerView.suggestNextEpisode(next);
        }
    }

    protected void playNextEpisode() {
        EpisodeModel next = getNextEpisode(mCurrentEpisode);
        Log.d(TAG, "Play next episode: " + next);
        if (next != null) {
            playEpisode(next);
        }
    }

    // VideoPlayer.Listener implementation

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        videoPlayerView.onStateChanged(playWhenReady, playbackState);
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