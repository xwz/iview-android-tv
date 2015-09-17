package io.github.xwz.base.adapters;

import android.content.Context;
import android.graphics.Point;
import android.support.v17.leanback.widget.BaseCardView;
import android.support.v17.leanback.widget.ImageCardView;
import android.view.View;
import android.view.ViewGroup;

import io.github.xwz.base.R;
import io.github.xwz.base.views.EpisodeCardView;

public class FilmPresenter extends EpisodePresenter {

    private boolean showDetails = false;

    public FilmPresenter() {

    }

    public FilmPresenter(boolean details) {
        showDetails = details;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        return new EpisodeCardView(context, getCardView(context), getCardSize(context), true);
    }

    @Override
    protected Point getCardSize(Context context) {
        return new Point(context.getResources().getDimensionPixelSize(R.dimen.poster_width),
                context.getResources().getDimensionPixelSize(R.dimen.poster_height));
    }

    @Override
    protected ImageCardView getCardView(Context context) {
        ImageCardView card = super.getCardView(context);
        card.setCardType(showDetails ? BaseCardView.CARD_TYPE_INFO_UNDER_WITH_EXTRA : BaseCardView.CARD_TYPE_MAIN_ONLY);
        return card;
    }
}
