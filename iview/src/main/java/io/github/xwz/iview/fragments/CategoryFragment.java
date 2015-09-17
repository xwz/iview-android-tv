package io.github.xwz.iview.fragments;

import io.github.xwz.base.content.IContentManager;
import io.github.xwz.iview.activities.DetailsActivity;
import io.github.xwz.iview.activities.SearchActivity;
import io.github.xwz.iview.content.ContentManager;

public class CategoryFragment extends io.github.xwz.base.fragments.CategoryFragment {
    @Override
    protected IContentManager getContentManger() {
        return ContentManager.getInstance();
    }

    @Override
    protected Class<?> getSearchActivityClass() {
        return SearchActivity.class;
    }

    @Override
    protected Class<?> getDetailsActivityClass() {
        return DetailsActivity.class;
    }
}
