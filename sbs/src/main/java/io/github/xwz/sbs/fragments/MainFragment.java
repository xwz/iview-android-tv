package io.github.xwz.sbs.fragments;

import android.support.v17.leanback.widget.BrowseFrameLayout;
import android.view.View;

import io.github.xwz.base.content.IContentManager;
import io.github.xwz.sbs.R;


public class MainFragment extends io.github.xwz.base.fragments.MainFragment {

    @Override
    protected IContentManager getContentManger() {
        return null;
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
        return null;
    }

    @Override
    protected Class<?> getDetailsActivityClass() {
        return null;
    }
}
