package io.github.xwz.sbs;

import android.app.Application;

import io.github.xwz.sbs.content.ContentManager;

public class MainApplication extends Application {
    private static final String TAG = "MainApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        new ContentManager(this).fetchShowList();
    }
}