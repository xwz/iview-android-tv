package io.github.xwz.base.views;

import android.content.Context;
import android.graphics.Point;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;

import com.squareup.picasso.Picasso;

import io.github.xwz.base.R;
import io.github.xwz.base.models.IEpisodeModel;

public class EpisodeCardView extends Presenter.ViewHolder {
    private static final String TAG = "EpisodeCardView";

    private final ImageCardView card;
    private final Context mContext;
    private final Point size;

    public EpisodeCardView(Context context, ImageCardView view) {
        super(view);
        mContext = context;
        card = view;
        size = new Point(mContext.getResources().getDimensionPixelSize(R.dimen.card_width),
                mContext.getResources().getDimensionPixelSize(R.dimen.card_height));
        card.setMainImageDimensions(size.x, size.y);
    }

    public void setEpisode(IEpisodeModel ep) {
        card.setTitleText(ep.getSeriesTitle());
        card.setContentText(ep.getTitle());
        if (ep.getEpisodeCount() > 0) {
            TextDrawable badge = new TextDrawable(mContext);
            badge.setText("" + ep.getEpisodeCount());
            card.setBadgeImage(badge);
        } else {
            card.setBadgeImage(null);
        }
        Picasso.with(mContext)
                .load(ep.getThumbnail())
                .resize(size.x, size.y)
                .into(card.getMainImageView());
    }

    public ImageCardView getImageCardView() {
        return card;
    }
}
