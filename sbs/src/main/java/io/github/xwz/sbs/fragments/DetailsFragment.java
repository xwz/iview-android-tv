package io.github.xwz.sbs.fragments;

import android.util.Log;

import io.github.xwz.base.api.EpisodeBaseModel;
import io.github.xwz.base.content.ContentManagerBase;
import io.github.xwz.sbs.activities.VideoPlayerActivity;
import io.github.xwz.sbs.content.ContentManager;

public class DetailsFragment extends io.github.xwz.base.fragments.DetailsFragment {

    @Override
    protected ContentManagerBase getContentManger() {
        return ContentManager.getInstance();
    }

    @Override
    protected Class<?> getPlayerActivityClass() {
        return VideoPlayerActivity.class;
    }

    protected void selectCurrentEpisode(EpisodeBaseModel episode, boolean initial) {
        if (!episode.equals(getCurrentEpisode())) {
            setCurrentEpisode(episode);
            if (initial) {
                getContentManger().fetchEpisode(episode);
            }
            if (getDetailView() != null) {
                getDetailView().setEpisode(episode);
            }
        }
    }
}
