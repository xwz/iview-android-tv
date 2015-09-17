package io.github.xwz.base.adapters;

import android.content.Context;
import android.graphics.Point;
import android.support.v17.leanback.widget.BaseCardView;
import android.support.v17.leanback.widget.ImageCardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import io.github.xwz.base.IApplication;
import io.github.xwz.base.R;
import io.github.xwz.base.views.EpisodeCardView;

public class FilmPresenter extends EpisodePresenter {

    private boolean large = false;

    public FilmPresenter() {

    }

    public FilmPresenter(boolean details) {
        large = details;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        return new EpisodeCardView(context, getCardView(context), getCardSize(context), true);
    }

    @Override
    protected Point getCardSize(Context context) {
        if (large) {
            return new Point(context.getResources().getDimensionPixelSize(R.dimen.poster_width_large),
                    context.getResources().getDimensionPixelSize(R.dimen.poster_height_large));
        } else {
            return new Point(context.getResources().getDimensionPixelSize(R.dimen.poster_width),
                    context.getResources().getDimensionPixelSize(R.dimen.poster_height));
        }
    }

    @Override
    protected ImageCardView getCardView(Context context) {
        ImageCardView card = super.getCardView(context);

        // nasty hack to hide content text view
        if (context.getApplicationContext() instanceof IApplication) {
            IApplication app = (IApplication) context.getApplicationContext();
            View details = card.findViewById(app.getImageCardViewContentTextResId());
            if (details != null) {
                details.setVisibility(View.GONE);
            }
            View info = card.findViewById(app.getImageCardViewInfoFieldResId());
            if (info != null && info instanceof RelativeLayout) {
                ViewGroup.LayoutParams lp = info.getLayoutParams();
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                info.setLayoutParams(lp);
            }
            View title = card.findViewById(app.getImageCardViewTitleTextResId());
            if (title != null && title.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) title.getLayoutParams();
                lp.setMargins(0, -5, 0, 10);
                title.setLayoutParams(lp);
            }
        }

        card.setInfoAreaBackgroundColor(context.getResources().getColor(R.color.black_900));
        card.setCardType(BaseCardView.CARD_TYPE_INFO_OVER);
        return card;
    }
}
