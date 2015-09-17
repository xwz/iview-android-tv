package io.github.xwz.base.adapters;

import android.content.Context;
import android.graphics.Point;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;
import android.widget.ImageView;

import io.github.xwz.base.R;
import io.github.xwz.base.api.EpisodeBaseModel;
import io.github.xwz.base.views.EpisodeCardView;

public class EpisodePresenter extends Presenter {
    private static final String TAG = "EpisodePresenter";

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        return new EpisodeCardView(context, getCardView(context), getCardSize(context), false);
    }

    protected Point getCardSize(Context context) {
        return new Point(context.getResources().getDimensionPixelSize(R.dimen.card_width),
                context.getResources().getDimensionPixelSize(R.dimen.card_height));
    }

    protected ImageCardView getCardView(Context context) {
        ImageCardView card = new ImageCardView(context);
        card.setFocusable(true);
        card.setFocusableInTouchMode(true);
        card.setMainImageScaleType(ImageView.ScaleType.CENTER_CROP);
        Point size = getCardSize(context);
        card.setMainImageDimensions(size.x, size.y);
        return card;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ((EpisodeCardView) viewHolder).setEpisode((EpisodeBaseModel) item);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }
}
