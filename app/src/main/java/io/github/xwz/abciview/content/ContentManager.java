package io.github.xwz.abciview.content;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.xwz.abciview.ImmutableMap;
import io.github.xwz.abciview.api.AuthApi;
import io.github.xwz.abciview.api.EpisodeDetailsApi;
import io.github.xwz.abciview.api.TvShowListApi;
import io.github.xwz.abciview.models.EpisodeModel;

/**
 * Created by wei on 27/08/15.
 */
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

    public static final Map<String, String> CHANNELS = ImmutableMap.of(
            "abc1", "ABC1",
            "abc2", "ABC2",
            "abc3", "ABC3",
            "abc4kids", "ABC4Kids",
            "iview", "iView Exclusives"
    );

    private static final String TAG = "ContentManager";

    public static final String CONTENT_ID = "io.github.xwz.abciview.CONTENT_ID";
    public static final String CONTENT_TAG = "io.github.xwz.abciview.CONTENT_TAG";

    public static final String CONTENT_SHOW_LIST_FETCHING = "io.github.xwz.abciview.CONTENT_SHOW_LIST_FETCHING";
    public static final String CONTENT_SHOW_LIST_START = "io.github.xwz.abciview.CONTENT_SHOW_LIST_START";
    public static final String CONTENT_SHOW_LIST_DONE = "io.github.xwz.abciview.CONTENT_SHOW_LIST_DONE";
    public static final String CONTENT_SHOW_LIST_ERROR = "io.github.xwz.abciview.CONTENT_SHOW_LIST_ERROR";

    public static final String CONTENT_EPISODE_FETCHING = "io.github.xwz.abciview.CONTENT_EPISODE_FETCHING";
    public static final String CONTENT_EPISODE_START = "io.github.xwz.abciview.CONTENT_EPISODE_START";
    public static final String CONTENT_EPISODE_DONE = "io.github.xwz.abciview.CONTENT_EPISODE_DONE";
    public static final String CONTENT_EPISODE_ERROR = "io.github.xwz.abciview.CONTENT_EPISODE_ERROR";

    public static final String CONTENT_AUTH_FETCHING = "io.github.xwz.abciview.CONTENT_AUTH_FETCHING";
    public static final String CONTENT_AUTH_START = "io.github.xwz.abciview.CONTENT_AUTH_START";
    public static final String CONTENT_AUTH_DONE = "io.github.xwz.abciview.CONTENT_AUTH_DONE";
    public static final String CONTENT_AUTH_ERROR = "io.github.xwz.abciview.CONTENT_AUTH_ERROR";

    public static final String AUTH_FAILED_NETWORK = "AUTH_FAILED_NETWORK";
    public static final String AUTH_FAILED_TOKEN = "AUTH_FAILED_TOKEN";
    public static final String AUTH_FAILED_URL = "AUTH_FAILED_URL";

    public static final String OTHER_EPISODES = "OTHER_EPISODES";

    private Context mContext = null;
    private ContentCacheManager mCache = null;

    static ContentManager instance = null;

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

    public void fetchShowList() {
        mCache.broadcastChange(CONTENT_SHOW_LIST_FETCHING);
        new TvShowListApi(mContext).execute();
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

    public List<String> suggestions(String query) {
        return mCache.getSuggestions(query);
    }

    public List<EpisodeModel> getAllShows() {
        return mCache.getAllShows();
    }

    public Map<String, List<EpisodeModel>> getAllShowsByCategories() {
        List<EpisodeModel> shows = mCache.getAllShows();
        Map<String, List<EpisodeModel>> all = new LinkedHashMap<>();
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
}
