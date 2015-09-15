package io.github.xwz.iview.content;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.xwz.iview.api.AuthApi;
import io.github.xwz.iview.api.EpisodeDetailsApi;
import io.github.xwz.iview.api.TvShowListApi;
import io.github.xwz.base.ImmutableMap;
import io.github.xwz.base.content.IContentManager;
import io.github.xwz.base.models.IEpisodeModel;

public class ContentManager implements IContentManager {

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

    private static final String CONTENT_SHOW_LIST_FETCHING = "io.github.xwz.iview.CONTENT_SHOW_LIST_FETCHING";
    private static final String CONTENT_EPISODE_FETCHING = "io.github.xwz.iview.CONTENT_EPISODE_FETCHING";
    private static final String CONTENT_AUTH_FETCHING = "io.github.xwz.iview.CONTENT_AUTH_FETCHING";

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

    @Override
    public void fetchAuthToken(IEpisodeModel episode) {
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

    @Override
    public void fetchEpisode(IEpisodeModel episode) {
        mCache.broadcastChange(CONTENT_EPISODE_FETCHING, episode.getHref());
        IEpisodeModel existing = mCache.getEpisode(episode.getHref());
        if (existing != null && existing.hasExtras() && existing.hasOtherEpisodes()) {
            mCache.broadcastChangeDelayed(100, CONTENT_EPISODE_DONE, episode.getHref(), null);
        } else {
            new EpisodeDetailsApi(mContext, episode.getHref()).execute(episode.getHref());
        }
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

    public LinkedHashMap<String, List<IEpisodeModel>> getAllShowsByCategories() {
        List<IEpisodeModel> shows = mCache.getAllShows();
        LinkedHashMap<String, List<IEpisodeModel>> all = new LinkedHashMap<>();
        all.putAll(mCache.getCollections());

        for (Map.Entry<String, String> channel : CHANNELS.entrySet()) {
            List<IEpisodeModel> episodes = new ArrayList<>();
            for (IEpisodeModel show : shows) {
                if (channel.getKey().equals(show.getChannel())) {
                    episodes.add(show);
                }
            }
            all.put(channel.getValue(), episodes);
        }
        for (Map.Entry<String, String> cat : CATEGORIES.entrySet()) {
            List<IEpisodeModel> episodes = new ArrayList<>();
            for (IEpisodeModel show : shows) {
                if (show.getCategories().contains(cat.getKey())) {
                    episodes.add(show);
                }
            }
            all.put(cat.getValue(), episodes);
        }
        return all;
    }

    public IEpisodeModel getEpisode(String href) {
        return mCache.getEpisode(href);
    }

    public Uri getEpisodeStreamUrl(IEpisodeModel episode) {
        return mCache.getEpisodeStreamUrl(episode.getHref());
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
            return ContentManager.getInstance().getEpisode(next);
        }
        return null;
    }

    public List<IEpisodeModel> getRecommendations() {
        List<IEpisodeModel> all = getAllShows();
        if (all.size() > 40) {
            return getAllShows().subList(30, 32);
        }
        return new ArrayList<>();
    }

    public void recommendEpisode(Context context, IEpisodeModel ep, RecommendationPosition position) {
        Intent intent = new Intent(context, RecommendationsService.class);
        intent.putExtra(ContentManager.CONTENT_ID, ep);
        intent.putExtra(ContentManager.CONTENT_TAG, position.getId());
        context.startService(intent);
    }

    public void updateRecommendations(Context context) {
        List<IEpisodeModel> shows = ContentManager.getInstance().getRecommendations();
        int i = 0;
        for (IEpisodeModel show : shows) {
            Log.d(TAG, "Recommendation: " + i + ", " + show);
            if (i < 2) {
                recommendEpisode(context, show, RecommendationPosition.values()[i++]);
            }
        }
    }
}
