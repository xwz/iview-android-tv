package io.github.xwz.sbs.content;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import io.github.xwz.base.content.ContentCacheManager;
import io.github.xwz.base.content.IContentManager;
import io.github.xwz.base.models.IEpisodeModel;
import io.github.xwz.sbs.api.SBSApi;
import io.github.xwz.sbs.api.SBSAuthApi;
import io.github.xwz.sbs.api.SBSRelatedApi;

public class ContentManager implements IContentManager {

    private static ContentManager instance = null;

    private Context mContext = null;
    private ContentCacheManager mCache = null;

    public ContentManager(Context context) {
        instance = this;
        mContext = context;
        mCache = new ContentCacheManager(context);
    }

    public static ContentManager getInstance() {
        return instance;
    }

    public static ContentCacheManager cache() {
        return getInstance().mCache;
    }

    public void broadcastChange(String change, String tag, String id) {
        mCache.broadcastChange(change, tag, id);
    }

    public void broadcastChange(String change, String tag) {
        mCache.broadcastChange(change, tag);
    }

    public void broadcastChange(String change) {
        mCache.broadcastChange(change);
    }

    @Override
    public void updateRecommendations(Context context) {

    }

    @Override
    public LinkedHashMap<String, List<IEpisodeModel>> getAllShowsByCategories() {
        return mCache.getCollections();
    }

    private SBSApi fetchShows;

    private long lastFetchList = 0;

    @Override
    public void fetchShowList(boolean force) {
        long now = (new Date()).getTime();
        boolean shouldFetch = force || now - lastFetchList > 1800000;
        Log.d("ContentManager", "diff:" + (now - lastFetchList));
        if (shouldFetch && (fetchShows == null || fetchShows.getStatus() == AsyncTask.Status.FINISHED)) {
            mCache.broadcastChange(CONTENT_SHOW_LIST_FETCHING);
            fetchShows = new SBSApi(mContext);
            fetchShows.execute();
            lastFetchList = now;
        }
    }

    @Override
    public IEpisodeModel getEpisode(String href) {
        return mCache.getEpisode(href);
    }

    @Override
    public void fetchEpisode(IEpisodeModel episode) {
        mCache.broadcastChange(CONTENT_EPISODE_FETCHING, episode.getHref());
        IEpisodeModel existing = mCache.getEpisode(episode.getHref());
        if (existing != null && existing.hasExtras() && existing.hasOtherEpisodes()) {
            mCache.broadcastChangeDelayed(100, CONTENT_EPISODE_DONE, episode.getHref(), null);
        } else {
            new SBSRelatedApi(mContext, episode.getHref()).execute(episode.getHref());
        }
    }

    @Override
    public List<String> suggestions(String query) {
        return null;
    }

    @Override
    public List<IEpisodeModel> searchShows(String query) {
        return null;
    }

    @Override
    public void fetchAuthToken(IEpisodeModel episode) {
        mCache.broadcastChange(CONTENT_AUTH_FETCHING, episode.getHref());
        new SBSAuthApi(mContext, episode.getHref()).execute(episode.getHref());
    }

    @Override
    public Uri getEpisodeStreamUrl(IEpisodeModel episode) {
        return mCache.getEpisodeStreamUrl(episode.getHref());
    }

    @Override
    public IEpisodeModel findNextEpisode(List<String> urls, String current) {
        return null;
    }
}
