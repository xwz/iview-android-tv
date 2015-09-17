package io.github.xwz.sbs.fragments;

import io.github.xwz.base.content.ContentManagerBase;
import io.github.xwz.sbs.R;
import io.github.xwz.sbs.activities.DetailsActivity;
import io.github.xwz.sbs.activities.SearchActivity;
import io.github.xwz.sbs.content.ContentManager;

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
