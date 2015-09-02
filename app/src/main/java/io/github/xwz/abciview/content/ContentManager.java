package io.github.xwz.abciview.content;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.xwz.abciview.ImmutableMap;
import io.github.xwz.abciview.api.AuthApi;
import io.github.xwz.abciview.api.EpisodeDetailsApi;
import io.github.xwz.abciview.api.TvShowListApi;
import io.github.xwz.abciview.models.EpisodeModel;

public class ContentManager {

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

    private static final String TAG = "ContentManager";

    public static final String CONTENT_ID = "io.github.xwz.abciview.CONTENT_ID";
    public static final String CONTENT_TAG = "io.github.xwz.abciview.CONTENT_TAG";

    private static final String CONTENT_SHOW_LIST_FETCHING = "io.github.xwz.abciview.CONTENT_SHOW_LIST_FETCHING";
    public static final String CONTENT_SHOW_LIST_START = "io.github.xwz.abciview.CONTENT_SHOW_LIST_START";
    public static final String CONTENT_SHOW_LIST_DONE = "io.github.xwz.abciview.CONTENT_SHOW_LIST_DONE";
    public static final String CONTENT_SHOW_LIST_ERROR = "io.github.xwz.abciview.CONTENT_SHOW_LIST_ERROR";

    private static final String CONTENT_EPISODE_FETCHING = "io.github.xwz.abciview.CONTENT_EPISODE_FETCHING";
    public static final String CONTENT_EPISODE_START = "io.github.xwz.abciview.CONTENT_EPISODE_START";
    public static final String CONTENT_EPISODE_DONE = "io.github.xwz.abciview.CONTENT_EPISODE_DONE";
    public static final String CONTENT_EPISODE_ERROR = "io.github.xwz.abciview.CONTENT_EPISODE_ERROR";

    private static final String CONTENT_AUTH_FETCHING = "io.github.xwz.abciview.CONTENT_AUTH_FETCHING";
    public static final String CONTENT_AUTH_START = "io.github.xwz.abciview.CONTENT_AUTH_START";
    public static final String CONTENT_AUTH_DONE = "io.github.xwz.abciview.CONTENT_AUTH_DONE";
    public static final String CONTENT_AUTH_ERROR = "io.github.xwz.abciview.CONTENT_AUTH_ERROR";

    public static final String AUTH_FAILED_NETWORK = "AUTH_FAILED_NETWORK";
    public static final String AUTH_FAILED_TOKEN = "AUTH_FAILED_TOKEN";
    public static final String AUTH_FAILED_URL = "AUTH_FAILED_URL";

    public static final String OTHER_EPISODES = "OTHER_EPISODES";
    public static final String GLOBAL_SEARCH_INTENT = "GLOBAL_SEARCH_INTENT";

    //The columns we'll include in the video database table
    public static final String KEY_SERIES_TITLE = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String KEY_TITLE = SearchManager.SUGGEST_COLUMN_TEXT_2;

    public static final String KEY_IMAGE = SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE;
    public static final String KEY_DATA_TYPE = SearchManager.SUGGEST_COLUMN_CONTENT_TYPE;
    public static final String KEY_IS_LIVE = SearchManager.SUGGEST_COLUMN_IS_LIVE;
    public static final String KEY_VIDEO_WIDTH = SearchManager.SUGGEST_COLUMN_VIDEO_WIDTH;
    public static final String KEY_VIDEO_HEIGHT = SearchManager.SUGGEST_COLUMN_VIDEO_HEIGHT;
    public static final String KEY_PRODUCTION_YEAR = SearchManager.SUGGEST_COLUMN_PRODUCTION_YEAR;
    public static final String KEY_COLUMN_DURATION = SearchManager.SUGGEST_COLUMN_DURATION;
    public static final String KEY_ACTION = SearchManager.SUGGEST_COLUMN_INTENT_ACTION;
    public static final String KEY_EXTRA_DATA = SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA;
    public static final String KEY_EXTRA_NAME = SearchManager.EXTRA_DATA_KEY;

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

    private Context mContext = null;
    private ContentCacheManager mCache = null;

    private static ContentManager instance = null;

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

    public void fetchAuthToken(EpisodeModel episode) {
        mCache.broadcastChange(CONTENT_AUTH_FETCHING, episode.getHref());
        new AuthApi(mContext, episode.getHref()).execute(episode.getStream());
    }

    private TvShowListApi fetchShows;

    public void fetchShowList() {
        if (fetchShows == null || fetchShows.getStatus() == AsyncTask.Status.FINISHED) {
            mCache.broadcastChange(CONTENT_SHOW_LIST_FETCHING);
            fetchShows = new TvShowListApi(mContext);
            fetchShows.execute();
        }
    }

    public void fetchEpisode(EpisodeModel episode) {
        mCache.broadcastChange(CONTENT_EPISODE_FETCHING, episode.getHref());
        EpisodeModel existing = mCache.getEpisode(episode.getHref());
        if (existing != null && existing.hasExtras() && existing.hasOtherEpisodes()) {
            mCache.broadcastChangeDelayed(100, CONTENT_EPISODE_DONE, episode.getHref(), null);
        } else {
            new EpisodeDetailsApi(mContext, episode.getHref()).execute(episode.getHref());
        }
    }

    public List<EpisodeModel> searchShows(String query) {
        List<EpisodeModel> results = new ArrayList<>();
        query = query.toLowerCase();
        for (EpisodeModel episode : mCache.getAllShows()) {
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
        for (EpisodeModel ep : searchShows(query)) {
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
            row.put(KEY_COLUMN_DURATION, ep.getDuration()*1000);
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

    public List<EpisodeModel> getAllShows() {
        return mCache.getAllShows();
    }

    public LinkedHashMap<String, List<EpisodeModel>> getAllShowsByCategories() {
        List<EpisodeModel> shows = mCache.getAllShows();
        LinkedHashMap<String, List<EpisodeModel>> all = new LinkedHashMap<>();
        all.putAll(mCache.getCollections());

        for (Map.Entry<String, String> channel : CHANNELS.entrySet()) {
            List<EpisodeModel> episodes = new ArrayList<>();
            for (EpisodeModel show : shows) {
                if (channel.getKey().equals(show.getChannel())) {
                    episodes.add(show);
                }
            }
            all.put(channel.getValue(), episodes);
        }
        for (Map.Entry<String, String> cat : CATEGORIES.entrySet()) {
            List<EpisodeModel> episodes = new ArrayList<>();
            for (EpisodeModel show : shows) {
                if (show.getCategories().contains(cat.getKey())) {
                    episodes.add(show);
                }
            }
            all.put(cat.getValue(), episodes);
        }
        return all;
    }

    public EpisodeModel getEpisode(String href) {
        return mCache.getEpisode(href);
    }

    public Uri getEpisodeStreamUrl(EpisodeModel episode) {
        return mCache.getEpisodeStreamUrl(episode.getHref());
    }

    public EpisodeModel findNextEpisode(List<String> urls, String current) {
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
            return ContentManager.getInstance().getEpisode(next);
        }
        return null;
    }

    public List<EpisodeModel> getRecommendations() {
        List<EpisodeModel> all = getAllShows();
        if (all.size() > 40) {
            return getAllShows().subList(30, 32);
        }
        return new ArrayList<>();
    }

    public void recommendEpisode(Context context, EpisodeModel ep, RecommendationPosition position) {
        Intent intent = new Intent(context, RecommendationsService.class);
        intent.putExtra(ContentManager.CONTENT_ID, ep);
        intent.putExtra(ContentManager.CONTENT_TAG, position.getId());
        context.startService(intent);
    }

    public void updateRecommendations(Context context) {
        List<EpisodeModel> shows = ContentManager.getInstance().getRecommendations();
        int i = 0;
        for (EpisodeModel show : shows) {
            Log.d(TAG, "Recommendation: " + i+", "+show);
            if (i < 2) {
                recommendEpisode(context, show, RecommendationPosition.values()[i++]);
            }
        }
    }
}
