package io.github.xwz.sbs.fragments;

import android.support.v17.leanback.widget.BrowseFrameLayout;
import android.view.View;

import io.github.xwz.base.content.ContentManagerBase;
import io.github.xwz.sbs.R;
import io.github.xwz.sbs.activities.CategoryActivity;
import io.github.xwz.sbs.activities.DetailsActivity;
import io.github.xwz.sbs.activities.SearchActivity;
import io.github.xwz.sbs.content.ContentManager;


public class MainFragment extends io.github.xwz.base.fragments.MainFragment {

    @Override
    protected ContentManagerBase getContentManger() {
        return ContentManager.getInstance();
    }

    @Override
    protected void setupHeader() {
        setBadgeDrawable(getResources().getDrawable(R.mipmap.logo));
        setSearchAffordanceColor(getResources().getColor(R.color.brand_color));
    }

    @Override
    protected BrowseFrameLayout getBrowseFrame(View root) {
        return (BrowseFrameLayout) root.findViewById(android.support.v17.leanback.R.id.browse_frame);
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
    protected Class<?> getCategoryActivityClass() {
        return CategoryActivity.class;
    }
}
