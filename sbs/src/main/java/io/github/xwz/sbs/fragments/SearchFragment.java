package io.github.xwz.sbs.fragments;

import io.github.xwz.base.content.ContentManagerBase;
import io.github.xwz.sbs.activities.DetailsActivity;
import io.github.xwz.sbs.content.ContentManager;


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