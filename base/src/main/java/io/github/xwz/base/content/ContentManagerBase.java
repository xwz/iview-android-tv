package io.github.xwz.base.content;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.xwz.base.api.ContentDatabaseCache;
import io.github.xwz.base.api.EpisodeBaseModel;
import io.github.xwz.base.api.EpisodeBaseModel$Table;
import io.github.xwz.base.api.PlayHistory;
import io.github.xwz.base.api.PlayHistory$Table;

public abstract class ContentManagerBase {

    public static final String CONTENT_ID = "io.github.xwz.base.CONTENT_ID";
    public static final String CONTENT_TAG = "io.github.xwz.base.CONTENT_TAG";

    public static final String CONTENT_SHOW_LIST_FETCHING = "io.github.xwz.base.CONTENT_SHOW_LIST_FETCHING";
    public static final String CONTENT_SHOW_LIST_START = "io.github.xwz.base.CONTENT_SHOW_LIST_START";
    public static final String CONTENT_SHOW_LIST_DONE = "io.github.xwz.base.CONTENT_SHOW_LIST_DONE";
    public static final String CONTENT_SHOW_LIST_ERROR = "io.github.xwz.base.CONTENT_SHOW_LIST_ERROR";
    public static final String CONTENT_SHOW_LIST_PROGRESS = "io.github.xwz.base.CONTENT_SHOW_LIST_PROGRESS";

    public static final String CONTENT_EPISODE_FETCHING = "io.github.xwz.base.CONTENT_EPISODE_FETCHING";
    public static final String CONTENT_EPISODE_START = "io.github.xwz.base.CONTENT_EPISODE_START";
    public static final String CONTENT_EPISODE_DONE = "io.github.xwz.base.CONTENT_EPISODE_DONE";
    public static final String CONTENT_EPISODE_ERROR = "io.github.xwz.base.CONTENT_EPISODE_ERROR";

    public static final String CONTENT_AUTH_FETCHING = "io.github.xwz.base.CONTENT_AUTH_FETCHING";
    public static final String CONTENT_AUTH_START = "io.github.xwz.base.CONTENT_AUTH_START";
    public static final String CONTENT_AUTH_DONE = "io.github.xwz.base.CONTENT_AUTH_DONE";
    public static final String CONTENT_AUTH_ERROR = "io.github.xwz.base.CONTENT_AUTH_ERROR";

    public static final String AUTH_FAILED_NETWORK = "AUTH_FAILED_NETWORK";
    public static final String AUTH_FAILED_TOKEN = "AUTH_FAILED_TOKEN";
    public static final String AUTH_FAILED_URL = "AUTH_FAILED_URL";

    public static final String OTHER_EPISODES = "OTHER_EPISODES";
    public static final String MORE_LIKE_THIS = "More Like This";
    public static final String GLOBAL_SEARCH_INTENT = "GLOBAL_SEARCH_INTENT";
    public static final String RECENTLY_PLAYED = "Recently played";

    //The columns we'll include in the video database table
    public static final String KEY_SERIES_TITLE = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String KEY_TITLE = SearchManager.SUGGEST_COLUMN_TEXT_2;

    public static final String KEY_IMAGE = SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE;
    public static final String KEY_DATA_TYPE = SearchManager.SUGGEST_COLUMN_CONTENT_TYPE;
    public static final String KEY_VIDEO_WIDTH = SearchManager.SUGGEST_COLUMN_VIDEO_WIDTH;
    public static final String KEY_VIDEO_HEIGHT = SearchManager.SUGGEST_COLUMN_VIDEO_HEIGHT;
    public static final String KEY_PRODUCTION_YEAR = SearchManager.SUGGEST_COLUMN_PRODUCTION_YEAR;
    public static final String KEY_COLUMN_DURATION = SearchManager.SUGGEST_COLUMN_DURATION;
    public static final String KEY_ACTION = SearchManager.SUGGEST_COLUMN_INTENT_ACTION;
    public static final String KEY_EXTRA_DATA = SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA;
    public static final String KEY_EXTRA_NAME = SearchManager.EXTRA_DATA_KEY;

    private static final String TAG = "ContentManagerBase";

    private static ContentManagerBase instance = null;

    private Context mContext = null;
    private ContentCacheManager mCache = null;
    private ContentDatabaseCache mDb = null;

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
        mDb = new ContentDatabaseCache();
    }

    public static ContentManagerBase getInstance() {
        return instance;
    }

    public static ContentCacheManager cache() {
        return getInstance().mCache;
    }

    public static ContentDatabaseCache db() {
        return getInstance().mDb;
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

    public List<EpisodeBaseModel> searchShows(String query) {
        List<EpisodeBaseModel> results = new ArrayList<>();
        query = query.toLowerCase();
        for (EpisodeBaseModel episode : mCache.getAllShows()) {
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
                KEY_VIDEO_WIDTH,
                KEY_VIDEO_HEIGHT,
                KEY_PRODUCTION_YEAR,
                KEY_COLUMN_DURATION,
                KEY_ACTION,
                KEY_EXTRA_DATA,
                KEY_EXTRA_NAME
        };
        MatrixCursor cursor = new MatrixCursor(columns);
        for (EpisodeBaseModel ep : searchShows(query)) {
            LinkedHashMap row = new LinkedHashMap();
            row.put(BaseColumns._ID, ep.getHref());
            row.put(KEY_SERIES_TITLE, ep.getSeriesTitle());
            row.put(KEY_TITLE, ep.getTitle());
            row.put(KEY_IMAGE, ep.getThumbnail());
            row.put(KEY_DATA_TYPE, "video/mp4");
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

    public List<EpisodeBaseModel> getAllShows() {
        return mCache.getAllShows();
    }

    public List<EpisodeBaseModel> getAllShowsByCategory(String cat) {
        List<EpisodeBaseModel> all = getAllShowsByCategories().get(cat);
        if (all == null) {
            all = new ArrayList<>();
        }
        return all;
    }

    public EpisodeBaseModel getEpisode(String href) {
        return mCache.getEpisode(href);
    }

    public Uri getEpisodeStreamUrl(EpisodeBaseModel episode) {
        return mCache.getEpisodeStreamUrl(episode.getHref());
    }

    public LinkedHashMap<String, List<EpisodeBaseModel>> getAllShowsByCategories() {
        return cache().getCollections();
    }

    public EpisodeBaseModel findNextEpisode(List<String> urls, String current) {
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

    public List<EpisodeBaseModel> getRecentlyPlayed() {

        FlowCursorList<PlayHistory> cursor = new FlowCursorList<>(false,
                (new Select()).from(PlayHistory.class)
                        .orderBy("CASE WHEN "+PlayHistory$Table.PROGRESS+" >= 75 THEN 1 ELSE 0 END ASC, "+PlayHistory$Table.TIMESTAMP+" DESC")
                        .limit(30));

        List<EpisodeBaseModel> recent = new ArrayList<>();
        for (int i = 0, k = cursor.getCount(); i < k; i++) {
            PlayHistory history = cursor.getItem(i);
            EpisodeBaseModel ep = getEpisode(history.href);
            if (ep != null) {
                if (history.progress < 75) {
                    ep.setResumePosition(history.position);
                }
                ep.setRecent(true);
                recent.add(ep);
            }
        }
        return recent;
    }

    public void recommendEpisode(Context context, EpisodeBaseModel ep, RecommendationPosition position) {
        Intent intent = new Intent(context, getRecommendationServiceClass());
        intent.putExtra(CONTENT_ID, ep);
        intent.putExtra(CONTENT_TAG, position.getId());
        context.startService(intent);
    }

    public void updateRecommendations(Context context) {
        List<EpisodeBaseModel> shows = getRecommendations();
        int i = 0;
        for (EpisodeBaseModel show : shows) {
            Log.d(TAG, "Recommendation: " + i + ", " + show);
            if (i < 2) {
                recommendEpisode(context, show, RecommendationPosition.values()[i++]);
            }
        }
    }

    public abstract void fetchShowList(boolean force);
    public abstract void fetchAuthToken(EpisodeBaseModel episode);
    public abstract void fetchEpisode(EpisodeBaseModel episode);

    public abstract List<EpisodeBaseModel> getRecommendations();
    public abstract Class getRecommendationServiceClass();
}