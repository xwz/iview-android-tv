package io.github.xwz.iview.activities;

import io.github.xwz.iview.content.ContentManager;
import io.github.xwz.iview.player.VideoPlayer;
import io.github.xwz.base.content.IContentManager;

/**
 * An activity that plays media using {@link VideoPlayer}.
 */
public class VideoPlayerActivity extends io.github.xwz.base.activities.VideoPlayerActivity {

    @Override
    protected IContentManager getContentManger() {
        return ContentManager.getInstance();
    }
}