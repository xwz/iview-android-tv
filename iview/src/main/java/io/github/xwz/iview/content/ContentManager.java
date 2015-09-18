package io.github.xwz.iview.content;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.xwz.base.ImmutableMap;
import io.github.xwz.base.api.EpisodeBaseModel;
import io.github.xwz.base.content.ContentManagerBase;
import io.github.xwz.iview.api.AuthApi;
import io.github.xwz.iview.api.EpisodeDetailsApi;
import io.github.xwz.iview.api.EpisodeModel;
import io.github.xwz.iview.api.TvShowListApi;

public class ContentManager extends ContentManagerBase {

    public static final Map<String, String> CATEGORIES = ImmutableMap.of(
            "arts", "Arts & Culture",
            "comedy", "Comedy",
            "docs", "Documentary",
            "drama", "Drama",
            "education", "Education",
            "lifestyle", "Lifestyle",
            "news", "News & Current Affairs",
            "panel", "Panel & Discussion",
            "sport", "Sport"
    );

    private static final Map<String, String> CHANNELS = ImmutableMap.of(
            "abc1", "ABC1",
            "abc2", "ABC2",
            "abc3", "ABC3",
            "abc4kids", "ABC4Kids",
            "iview", "iView Exclusives"
    );

    public ContentManager(Context context) {
        super(context);
    }

    private TvShowListApi fetchShows;
    private long lastFetchList = 0;

    @Override
    public void fetchShowList(boolean force) {
        long now = (new Date()).getTime();
        boolean shouldFetch = force || now - lastFetchList > 1800000;
        if (shouldFetch && (fetchShows == null || fetchShows.getStatus() == AsyncTask.Status.FINISHED)) {
            broadcastChange(CONTENT_SHOW_LIST_FETCHING);
            fetchShows = new TvShowListApi(getContext());
            fetchShows.execute();
            lastFetchList = now;
        }
    }

    @Override
    public void fetchAuthToken(EpisodeBaseModel episode) {
        EpisodeModel ep = (EpisodeModel)episode;
        cache().broadcastChange(CONTENT_AUTH_FETCHING, ep.getHref());
        new AuthApi(getContext(), episode.getHref()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ep.getStream());
    }

    @Override
    public void fetchEpisode(EpisodeBaseModel episode) {
        broadcastChange(CONTENT_EPISODE_FETCHING, episode.getHref());
        EpisodeModel existing = (EpisodeModel)getEpisode(episode.getHref());
        if (existing != null && existing.hasExtras() && existing.hasOtherEpisodes()) {
            cache().broadcastChangeDelayed(100, CONTENT_EPISODE_DONE, episode.getHref(), null);
        } else {
            new EpisodeDetailsApi(getContext(), episode.getHref()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, episode.getHref());
        }
    }

    @Override
    public LinkedHashMap<String, List<EpisodeBaseModel>> getAllShowsByCategories() {
        LinkedHashMap<String, List<EpisodeBaseModel>> all = super.getAllShowsByCategories();
        List<EpisodeBaseModel> shows = getAllShows();
        for (Map.Entry<String, String> channel : CHANNELS.entrySet()) {
            List<EpisodeBaseModel> episodes = new ArrayList<>();
            for (EpisodeBaseModel show : shows) {
                if (channel.getKey().equals(show.getChannel())) {
                    episodes.add(show);
                }
            }
            all.put(channel.getValue(), episodes);
        }
        for (Map.Entry<String, String> cat : CATEGORIES.entrySet()) {
            List<EpisodeBaseModel> episodes = new ArrayList<>();
            for (EpisodeBaseModel show : shows) {
                if (show.getCategories().contains(cat.getKey())) {
                    episodes.add(show);
                }
            }
            all.put(cat.getValue(), episodes);
        }
        return all;
    }

    @Override
    public List<EpisodeBaseModel> getRecommendations() {
        List<EpisodeBaseModel> all = getAllShows();
        if (all.size() > 40) {
            return getAllShows().subList(30, 32);
        }
        return new ArrayList<>();
    }

    @Override
    public Class getRecommendationServiceClass() {
        return RecommendationsService.class;
    }
}
