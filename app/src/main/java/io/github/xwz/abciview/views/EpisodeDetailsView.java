package io.github.xwz.abciview.views;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.support.v17.leanback.widget.Presenter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import io.github.xwz.abciview.R;
import io.github.xwz.abciview.models.EpisodeModel;
import jp.wasabeef.picasso.transformations.gpu.VignetteFilterTransformation;

/**
 * Created by wei on 28/08/15.
 */
public class EpisodeDetailsView extends Presenter.ViewHolder {
    private final Context mContext;

    private static final int LOADER_DELAY_MS = 200;

    private TextView episodeTitle;
    private TextView seriesTitle;
    private TextView description;
    private TextView duration;
    private ImageView image;

    private Transformation transformation;
    private Handler handler = new Handler();
    private EpisodeLoader loader;

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

    public void setEpisode(EpisodeModel episode) {
        episodeTitle.setText(episode.getTitle());
        seriesTitle.setText(episode.getSeriesTitle());
        duration.setText(episode.getDurationText());
        description.setText("");

        // load more expensive details after some delay
        loadEpisodeDetails(episode);
    }

    public void updateEpisode(EpisodeModel episode) {
        description.setText(episode.getDescription());
    }

    protected void loadEpisodeDetails(EpisodeModel episode) {
        loader.setEpisode(episode);
        handler.removeCallbacks(loader);
        handler.postDelayed(loader, LOADER_DELAY_MS);
    }

    private class EpisodeLoader implements Runnable {
        private EpisodeModel episode;

        public void setEpisode(EpisodeModel ep) {
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
