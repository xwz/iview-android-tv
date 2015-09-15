package io.github.xwz.sbs.content;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import java.util.LinkedHashMap;
import java.util.List;

import io.github.xwz.base.content.ContentCacheManager;
import io.github.xwz.base.content.IContentManager;
import io.github.xwz.base.models.IEpisodeModel;
import io.github.xwz.sbs.api.SBSApi;

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

    @Override
    public void updateRecommendations(Context context) {

    }

    @Override
    public LinkedHashMap<String, List<IEpisodeModel>> getAllShowsByCategories() {
        return null;
    }

    private SBSApi fetchShows;

    @Override
    public void fetchShowList() {
        if (fetchShows == null || fetchShows.getStatus() == AsyncTask.Status.FINISHED) {
            mCache.broadcastChange(CONTENT_SHOW_LIST_FETCHING);
            fetchShows = new SBSApi(mContext);
            fetchShows.execute();
        }
    }

    @Override
    public IEpisodeModel getEpisode(String href) {
        return null;
    }

    @Override
    public void fetchEpisode(IEpisodeModel episode) {

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

    }

    @Override
    public Uri getEpisodeStreamUrl(IEpisodeModel episode) {
        return null;
    }

    @Override
    public IEpisodeModel findNextEpisode(List<String> urls, String current) {
        return null;
    }
}
