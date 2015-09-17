package io.github.xwz.base.views;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.support.v17.leanback.widget.Presenter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import io.github.xwz.base.R;
import io.github.xwz.base.api.EpisodeBaseModel;
import jp.wasabeef.picasso.transformations.gpu.VignetteFilterTransformation;

public class EpisodeDetailsView extends Presenter.ViewHolder {
    private final Context mContext;

    private static final int LOADER_DELAY_MS = 200;

    private final TextView episodeTitle;
    private final TextView seriesTitle;
    private final TextView description;
    private final TextView duration;
    private final ImageView image;

    private final Transformation transformation;
    private final Handler handler = new Handler();
    private final EpisodeLoader loader;

    public EpisodeDetailsView(Context context, View view) {
        super(view);
        mContext = context;

        episodeTitle = (TextView) view.findViewById(R.id.episode_title);
        seriesTitle = (TextView) view.findViewById(R.id.series_title);
        description = (TextView) view.findViewById(R.id.description);
        duration = (TextView) view.findViewById(R.id.duration);
        image = (ImageView) view.findViewById(R.id.image);

        transformation = new VignetteFilterTransformation(mContext,
                new PointF(0.65f, 0.5f), new float[]{0.0f, 0.0f, 0.0f}, 0.2f, 0.85f);
        loader = new EpisodeLoader();
    }

    public void setEpisode(EpisodeBaseModel episode) {
        episodeTitle.setText(episode.getTitle());
        seriesTitle.setText(episode.getSeriesTitle());
        duration.setText(episode.getDurationText());

        if (sameTitles(episode.getSeriesTitle(), episode.getTitle())) {
            seriesTitle.setText("");
        }

        // load more expensive details after some delay
        loadEpisodeDetails(episode);
        if (episode.getDescription() != null && episode.getDescription().length() > 0) {
            description.setText(episode.getDescription());
        } else {
            description.setText("");
        }
    }

    private boolean sameTitles(String a, String b) {
        if (a != null && b != null) {
            return a.toLowerCase().equals(b.toLowerCase());
        }
        return false;
    }

    public void updateEpisode(EpisodeBaseModel episode) {
        description.setText(episode.getDescription());
    }

    private void loadEpisodeDetails(EpisodeBaseModel episode) {
        loader.setEpisode(episode);
        handler.removeCallbacks(loader);
        handler.postDelayed(loader, LOADER_DELAY_MS);
    }

    private class EpisodeLoader implements Runnable {
        private EpisodeBaseModel episode;

        public void setEpisode(EpisodeBaseModel ep) {
            episode = ep;
        }

        @Override
        public void run() {
            Picasso.with(mContext)
                    .load(episode.getThumbnail())
                    .fit()
                    .centerCrop()
                    .transform(transformation)
                    .into(image);
        }
    }
}
