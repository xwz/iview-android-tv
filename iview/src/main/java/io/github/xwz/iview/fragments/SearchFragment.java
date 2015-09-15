package io.github.xwz.iview.fragments;

import io.github.xwz.iview.activities.DetailsActivity;
import io.github.xwz.iview.content.ContentManager;
import io.github.xwz.base.content.IContentManager;

public class SearchFragment extends io.github.xwz.base.fragments.SearchFragment {

    @Override
    protected IContentManager getContentManger() {
        return ContentManager.getInstance();
    }

    @Override
    protected Class<?> getDetailsActivityClass() {
        return DetailsActivity.class;
    }
}