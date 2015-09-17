package io.github.xwz.iview.fragments;

import io.github.xwz.base.content.ContentManagerBase;
import io.github.xwz.iview.R;
import io.github.xwz.iview.activities.DetailsActivity;
import io.github.xwz.iview.activities.SearchActivity;
import io.github.xwz.iview.content.ContentManager;

public class CategoryFragment extends io.github.xwz.base.fragments.CategoryFragment {
    @Override
    protected ContentManagerBase getContentManger() {
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

    @Override
    protected void setupHeader() {
        setSearchAffordanceColor(getResources().getColor(R.color.brand_color));
    }
}
