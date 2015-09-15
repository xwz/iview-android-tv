package io.github.xwz.base.player;

import android.os.Handler;
import android.util.Log;
import android.util.Pair;

import com.google.android.exoplayer.ExoPlayer;

import java.util.ArrayList;
import java.util.List;

public class DurationLogger implements Runnable, VideoPlayer.Listener {

    private static final String TAG = "VideoTimeLogger";
    private static final int REFRESH_INTERVAL_MS = 1000;
    private final Handler handler = new Handler();
    private VideoPlayer player;
    private int state;

    private List<Pair<Long, OnTimeReached>> listeners = new ArrayList<>();

    public void start(VideoPlayer player) {
        this.stop();
        this.player = player;
        this.run();
    }

    private void stop() {
        handler.removeCallbacks(this);
        player = null;
    }

    public void endSession() {
        stop();
        listeners = new ArrayList<>();
    }

    /**
     * Listen for time remaining, in seconds, during play back.
     *
     * @param remaining seconds remaining
     * @param listener listener
     */
    public void addListener(long remaining, OnTimeReached listener) {
        listeners.add(new Pair<>(remaining, listener));
    }

    @Override
    public void run() {
        if (player != null) {
            //Log.d(TAG, "duration: " + player.getDuration() / 1000 + ", position:" + player.getCurrentPosition() / 1000);
            executeListeners();
        }
        handler.postDelayed(this, REFRESH_INTERVAL_MS);
    }

    private void executeListeners() {
        long duration = player.getDuration() / 1000;
        long position = player.getCurrentPosition() / 1000;
        List<Pair<Long, OnTimeReached>> executed = new ArrayList<>();
        if (duration > 0 && (state == ExoPlayer.STATE_READY || state == ExoPlayer.STATE_ENDED)) {
            long remain = duration - position;
            for (Pair<Long, OnTimeReached> listener : listeners) {
                long condition = listener.first;
                if (remain <= condition) {
                    Log.d(TAG, "Executing listener, duration=" + duration + ", position=" + position + ", remaining=" + remain);
                    listener.second.onPositionRemainingReached(duration, position);
                    executed.add(listener);
                }
            }
        }
        listeners.removeAll(executed);
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        state = playbackState;
        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                text += "buffering";
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                break;
            case ExoPlayer.STATE_IDLE:
                text += "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
                text += "preparing";
                break;
            case ExoPlayer.STATE_READY:
                text += "ready";
                break;
            default:
                text += "unknown";
                break;
        }
        Log.d(TAG, text);
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, float pixelWidthHeightRatio) {

    }

    public interface OnTimeReached {
        void onPositionRemainingReached(long duration, long position);
    }
}
