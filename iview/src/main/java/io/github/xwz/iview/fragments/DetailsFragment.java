package io.github.xwz.iview.fragments;

import io.github.xwz.base.content.ContentManagerBase;
import io.github.xwz.iview.activities.VideoPlayerActivity;
import io.github.xwz.iview.content.ContentManager;

public class DetailsFragment extends io.github.xwz.base.fragments.DetailsFragment {

    @Override
    protected ContentManagerBase getContentManger() {
        return ContentManager.getInstance();
    }

    @Override
    protected Class<?> getPlayerActivityClass() {
        return VideoPlayerActivity.class;
    }
}
