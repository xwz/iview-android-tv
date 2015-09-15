package io.github.xwz.sbs.content;

import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

public class ContentCacheManager {
    private final LocalBroadcastManager mBroadcastManager;

    public ContentCacheManager(Context context) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

}
