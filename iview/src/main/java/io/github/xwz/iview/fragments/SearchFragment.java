package io.github.xwz.iview.fragments;

import io.github.xwz.base.content.ContentManagerBase;
import io.github.xwz.iview.activities.DetailsActivity;
import io.github.xwz.iview.content.ContentManager;

public class SearchFragment extends io.github.xwz.base.fragments.SearchFragment {

    @Override
    protected ContentManagerBase getContentManger() {
        return ContentManager.getInstance();
    }

    @Override
    protected Class<?> getDetailsActivityClass() {
        return DetailsActivity.class;
    }
}