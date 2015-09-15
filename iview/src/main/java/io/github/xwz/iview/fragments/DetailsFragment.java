package io.github.xwz.iview.fragments;

import io.github.xwz.iview.activities.VideoPlayerActivity;
import io.github.xwz.iview.content.ContentManager;
import io.github.xwz.base.content.IContentManager;

public class DetailsFragment extends io.github.xwz.base.fragments.DetailsFragment {

    @Override
    protected IContentManager getContentManger() {
        return ContentManager.getInstance();
    }

    @Override
    protected Class<?> getPlayerActivityClass() {
        return VideoPlayerActivity.class;
    }
}
