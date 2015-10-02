package io.github.xwz.sbs.content;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.github.xwz.base.api.EpisodeBaseModel;
import io.github.xwz.base.content.ContentManagerBase;
import io.github.xwz.sbs.api.EpisodeModel;
import io.github.xwz.sbs.api.SBSApi;
import io.github.xwz.sbs.api.SBSAuthApi;
import io.github.xwz.sbs.api.SBSRelatedApi;

public class ContentManager extends ContentManagerBase {

    private static final String TAG = "ContentManager";

    private SBSApi fetchShows;

    private long lastFetchList = 0;

    public ContentManager(Context context) {
        super(context);
    }

    @Override
    public void fetchShowList(boolean force) {
        long now = (new Date()).getTime();
        boolean shouldFetch = force || now - lastFetchList > 1800000;
        Log.d(TAG, "diff:" + (now - lastFetchList));
        if (shouldFetch && (fetchShows == null || fetchShows.getStatus() == AsyncTask.Status.FINISHED)) {
            cache().broadcastChange(CONTENT_SHOW_LIST_FETCHING);
            fetchShows = new SBSApi(getContext());
            fetchShows.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            lastFetchList = now;
        }
    }

    @Override
    public void fetchEpisode(EpisodeBaseModel episode) {
        broadcastChange(CONTENT_EPISODE_FETCHING, episode.getHref());
        EpisodeModel existing = (EpisodeModel) cache().getEpisode(episode.getHref());
        if (existing != null && existing.hasExtras() && existing.hasOtherEpisodes()) {
            cache().broadcastChangeDelayed(100, CONTENT_EPISODE_DONE, episode.getHref(), null);
        } else {
            new SBSRelatedApi(getContext(), episode.getHref()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, episode.getHref());
        }
    }

    @Override
    public void fetchAuthToken(EpisodeBaseModel episode) {
        Log.d(TAG, "fetchAuthToken");
        cache().broadcastChange(CONTENT_AUTH_FETCHING, episode.getHref());
        new SBSAuthApi(getContext(), episode.getHref()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, episode.getHref());
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
