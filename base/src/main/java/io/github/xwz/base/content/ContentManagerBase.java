package io.github.xwz.base.content;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.github.xwz.base.api.IEpisodeModel;

public abstract class ContentManagerBase implements IContentManager {

    private static final String TAG = "ContentManagerBase";

    private static ContentManagerBase instance = null;

    private Context mContext = null;
    private ContentCacheManager mCache = null;

    public enum RecommendationPosition {
        FIRST(0), SECOND(1);
        private final int id;

        private RecommendationPosition(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public ContentManagerBase(Context context) {
        instance = this;
        mContext = context;
        mCache = new ContentCacheManager(context);
    }

    public static ContentManagerBase getInstance() {
        return instance;
    }

    public static ContentCacheManager cache() {
        return getInstance().mCache;
    }

    protected Context getContext() {
        return mContext;
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

    public List<IEpisodeModel> searchShows(String query) {
        List<IEpisodeModel> results = new ArrayList<>();
        query = query.toLowerCase();
        for (IEpisodeModel episode : mCache.getAllShows()) {
            if (episode.matches(query)) {
                results.add(episode);
            }
        }
        Log.d(TAG, "Search: '" + query + "' found : " + results.size());
        return results;
    }

    public Cursor searchShowsCursor(String query) {
        String[] columns = new String[]{
                BaseColumns._ID,
                KEY_SERIES_TITLE,
                KEY_TITLE,
                KEY_IMAGE,
                KEY_DATA_TYPE,
                KEY_IS_LIVE,
                KEY_VIDEO_WIDTH,
                KEY_VIDEO_HEIGHT,
                KEY_PRODUCTION_YEAR,
                KEY_COLUMN_DURATION,
                KEY_ACTION,
                KEY_EXTRA_DATA,
                KEY_EXTRA_NAME
        };
        MatrixCursor cursor = new MatrixCursor(columns);
        for (IEpisodeModel ep : searchShows(query)) {
            LinkedHashMap row = new LinkedHashMap();
            row.put(BaseColumns._ID, ep.getHref());
            row.put(KEY_SERIES_TITLE, ep.getSeriesTitle());
            row.put(KEY_TITLE, ep.getTitle());
            row.put(KEY_IMAGE, ep.getThumbnail());
            row.put(KEY_DATA_TYPE, "video/mp4");
            row.put(KEY_IS_LIVE, ep.getLivestream());
            row.put(KEY_VIDEO_WIDTH, 1280);
            row.put(KEY_VIDEO_HEIGHT, 720);
            row.put(KEY_PRODUCTION_YEAR, 2015);
            row.put(KEY_COLUMN_DURATION, ep.getDuration() * 1000);
            row.put(KEY_ACTION, GLOBAL_SEARCH_INTENT);
            row.put(KEY_EXTRA_DATA, ep.getHref());
            row.put(KEY_EXTRA_NAME, KEY_EXTRA_DATA);
            cursor.addRow(row.values());
        }
        return cursor;
    }

    public List<String> suggestions(String query) {
        return mCache.getSuggestions(query);
    }

    public List<IEpisodeModel> getAllShows() {
        return mCache.getAllShows();
    }

    public IEpisodeModel getEpisode(String href) {
        return mCache.getEpisode(href);
    }

    public Uri getEpisodeStreamUrl(IEpisodeModel episode) {
        return mCache.getEpisodeStreamUrl(episode.getHref());
    }

    public LinkedHashMap<String, List<IEpisodeModel>> getAllShowsByCategories() {
        return cache().getCollections();
    }

    public IEpisodeModel findNextEpisode(List<String> urls, String current) {
        String next = null;
        boolean found = false;
        for (String href : urls) {
            if (found) {
                next = href;
                break;
            }
            found = href.equals(current);
        }
        if (!found && next == null && urls.size() > 0) {
            next = urls.get(0);
        }
        if (next != null) {
            return getEpisode(next);
        }
        return null;
    }

    public void recommendEpisode(Context context, IEpisodeModel ep, RecommendationPosition position) {
        Intent intent = new Intent(context, getRecommendationServiceClass());
        intent.putExtra(IContentManager.CONTENT_ID, ep);
        intent.putExtra(IContentManager.CONTENT_TAG, position.getId());
        context.startService(intent);
    }

    public void updateRecommendations(Context context) {
        List<IEpisodeModel> shows = getRecommendations();
        int i = 0;
        for (IEpisodeModel show : shows) {
            Log.d(TAG, "Recommendation: " + i + ", " + show);
            if (i < 2) {
                recommendEpisode(context, show, RecommendationPosition.values()[i++]);
            }
        }
    }
}
