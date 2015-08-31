package io.github.xwz.abciview.adapters;

import android.graphics.Point;
import android.support.v17.leanback.widget.Presenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.xwz.abciview.R;
import io.github.xwz.abciview.models.EpisodeModel;
import io.github.xwz.abciview.views.EpisodeDetailsView;

/**
 * Created by wei on 28/08/15.
 */
public class EpisodeDetailsPresenter extends Presenter {

    private final Point mSize;

    public EpisodeDetailsPresenter(Point size) {
        mSize = size;
    }

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.episode_details_view, parent, false);
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.width = mSize.x;
        lp.height = mSize.y;
        view.setLayoutParams(lp);
        return new EpisodeDetailsView(parent.getContext(), view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ((EpisodeDetailsView) viewHolder).setEpisode((EpisodeModel) item);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
