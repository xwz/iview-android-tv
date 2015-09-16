package io.github.xwz.base.views;

import android.content.Context;
import android.graphics.Point;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import io.github.xwz.base.api.IEpisodeModel;

public class EpisodeCardView extends Presenter.ViewHolder {
    private static final String TAG = "EpisodeCardView";

    private final ImageCardView card;
    private final Context mContext;
    private final Point size;

    public EpisodeCardView(Context context, ImageCardView view, Point s) {
        super(view);
        mContext = context;
        card = view;
        size = s;
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
        String image = ep.getThumbnail();
        if (ep.hasCover()) {
            image = ep.getCover();
        }
        Picasso.with(mContext)
                .load(image)
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
