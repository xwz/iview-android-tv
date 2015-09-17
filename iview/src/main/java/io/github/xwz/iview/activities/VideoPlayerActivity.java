package io.github.xwz.iview.activities;

import io.github.xwz.base.content.ContentManagerBase;
import io.github.xwz.iview.content.ContentManager;

public class VideoPlayerActivity extends io.github.xwz.base.activities.VideoPlayerActivity {

    @Override
    protected ContentManagerBase getContentManger() {
        return ContentManager.getInstance();
    }

    @Override
    protected Class getVideoPlayerActivityClass() {
        return VideoPlayerActivity.class;
    }
}