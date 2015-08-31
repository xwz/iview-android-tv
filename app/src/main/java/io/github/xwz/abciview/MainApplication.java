package io.github.xwz.abciview;

import android.app.Application;

import io.github.xwz.abciview.content.ContentManager;

/**
 * Created by wei on 27/08/15.
 */
public class MainApplication extends Application {
    private static final String TAG = "MainApplication";
    private static ContentManager mContentManager = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mContentManager = new ContentManager(this);
    }
}
