package io.github.xwz.iview.fragments;

import android.support.v17.leanback.widget.BrowseFrameLayout;
import android.view.View;

import io.github.xwz.iview.R;
import io.github.xwz.iview.activities.DetailsActivity;
import io.github.xwz.iview.activities.SearchActivity;
import io.github.xwz.iview.content.ContentManager;
import io.github.xwz.base.content.IContentManager;

public class MainFragment extends io.github.xwz.base.fragments.MainFragment {

    @Override
    protected IContentManager getContentManger() {
        return ContentManager.getInstance();
    }

    @Override
    protected void setupHeader() {
        setBadgeDrawable(getResources().getDrawable(R.mipmap.logo));
        setSearchAffordanceColor(getResources().getColor(R.color.green_500));
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
}
