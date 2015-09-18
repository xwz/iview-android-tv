package io.github.xwz.base.api;

import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;

@Table(databaseName = ContentDatabase.NAME)
public class PlayHistory extends BaseModel {

    private static final String TAG = "PlayHistory";

    @Column
    @PrimaryKey
    public String href;

    @Column
    public String action;

    @Column
    public long duration;

    @Column
    public long position;

    @Column
    public long progress;

    @Column
    public long timestamp;

    public String toString() {
        return "PlayHistory:" + href + ", duration=" + duration + ", position=" + position + ", progress=" + progress;
    }

    public static void updateProgress(EpisodeBaseModel ep, long position) {
        PlayHistory history = new PlayHistory();
        history.href = ep.getHref();
        history.timestamp = (new Date()).getTime();
        history.duration = ep.getDuration() * 1000;
        history.position = position;
        if (history.duration > 0) {
            history.progress = Math.round(100 * (float) position / history.duration);
        } else {
            history.progress = 0;
        }
        history.save();
        Log.d(TAG, "Saving " + history);
    }
}
