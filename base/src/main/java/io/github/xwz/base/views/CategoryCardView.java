package io.github.xwz.base.views;

import android.content.Context;
import android.graphics.Point;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import io.github.xwz.base.R;
import io.github.xwz.base.Utils;
import io.github.xwz.base.api.EpisodeBaseModel;

public class CategoryCardView extends Presenter.ViewHolder {

    private Context mContext;
    private TextView title;
    private TextView badge;

    public CategoryCardView(Context context, ImageCardView view) {
        super(view);
        mContext = context;
        View layout = loadCategoryView(context, view);
        title = (TextView) layout.findViewById(R.id.title);
        badge = (TextView) layout.findViewById(R.id.title_badge);
    }

    private View loadCategoryView(Context context, ViewGroup parent) {
        Point size = new Point(mContext.getResources().getDimensionPixelSize(R.dimen.poster_width),
                mContext.getResources().getDimensionPixelSize(R.dimen.card_height));
        int infoHeight = context.getResources().getDimensionPixelSize(R.dimen.lb_basic_card_info_height);
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.category_card_view, parent, false);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) layout.getLayoutParams();
        lp.height = size.y + infoHeight;
        lp.width = size.x;
        layout.setLayoutParams(lp);
        parent.removeAllViews();
        parent.addView(layout);
        return layout;
    }

    public void setEpisode(EpisodeBaseModel ep) {
        title.setText(Utils.stripCategory(ep.getTitle()));
        badge.setText("" + ep.getEpisodeCount());
    }
}
