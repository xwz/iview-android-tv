package io.github.xwz.base.views;

import android.content.Context;
import android.graphics.Point;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.widget.ImageView;

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
        card.setMainImageScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    public void setEpisode(IEpisodeModel ep) {
        String series = ep.getSeriesTitle();
        String title = ep.getTitle();
        if (series == null || series.length() == 0) {
            series = title;
        }
        card.setTitleText(series);
        card.setContentText(title);
        if (sameTitles(series, title)) {
            card.setContentText("");
        }

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

    private boolean sameTitles(String a, String b) {
        if (a != null && b != null) {
            return a.toLowerCase().equals(b.toLowerCase());
        }
        return false;
    }

    public ImageCardView getImageCardView() {
        return card;
    }
}
