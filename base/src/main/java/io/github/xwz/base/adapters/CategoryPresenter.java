package io.github.xwz.base.adapters;

import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;

import io.github.xwz.base.api.EpisodeBaseModel;
import io.github.xwz.base.views.CategoryCardView;

public class CategoryPresenter extends Presenter {
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        ImageCardView card = new ImageCardView(parent.getContext());
        card.setFocusable(true);
        card.setFocusableInTouchMode(true);
        return new CategoryCardView(parent.getContext(), card);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ((CategoryCardView) viewHolder).setEpisode((EpisodeBaseModel) item);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
