package io.github.xwz.abciview;

import android.app.Application;

import io.github.xwz.abciview.content.ContentManager;

public class MainApplication extends Application {
    private static final String TAG = "MainApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        new ContentManager(this);
    }
}
