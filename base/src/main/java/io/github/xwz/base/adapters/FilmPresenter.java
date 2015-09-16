package io.github.xwz.base.adapters;

import android.content.Context;
import android.graphics.Point;
import android.support.v17.leanback.widget.BaseCardView;
import android.support.v17.leanback.widget.ImageCardView;
import android.view.View;
import android.view.ViewGroup;

import io.github.xwz.base.R;

public class FilmPresenter extends EpisodePresenter {
    @Override
    protected Point getCardSize(Context context) {
        return new Point(context.getResources().getDimensionPixelSize(R.dimen.poster_width),
                context.getResources().getDimensionPixelSize(R.dimen.poster_height));
    }

    @Override
    protected ImageCardView getCardView(Context context) {
        ImageCardView card = super.getCardView(context);
        card.setCardType(BaseCardView.CARD_TYPE_MAIN_ONLY);
        return card;
    }
}
