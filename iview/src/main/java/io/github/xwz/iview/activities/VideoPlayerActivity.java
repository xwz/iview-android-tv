package io.github.xwz.iview.activities;

import io.github.xwz.base.content.IContentManager;
import io.github.xwz.iview.content.ContentManager;

public class VideoPlayerActivity extends io.github.xwz.base.activities.VideoPlayerActivity {

    @Override
    protected IContentManager getContentManger() {
        return ContentManager.getInstance();
    }
}