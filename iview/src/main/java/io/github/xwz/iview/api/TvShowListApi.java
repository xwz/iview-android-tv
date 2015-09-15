package io.github.xwz.iview.api;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.xwz.iview.content.ContentManager;
import io.github.xwz.iview.models.EpisodeModel;
import io.github.xwz.base.ImmutableMap;
import io.github.xwz.base.models.IEpisodeModel;
import io.github.xwz.base.trie.RadixTree;

public class TvShowListApi extends IViewApi {
    private static final String TAG = "TvShowListApi";
    private static final int CACHE_EXPIRY = 600; // 10 mins

    private final Map<String, IEpisodeModel> episodes = new HashMap<>();
    private final List<IEpisodeModel> shows = new ArrayList<>();
    private LinkedHashMap<String, List<IEpisodeModel>> collections = new LinkedHashMap<>();
    private boolean success = false;

    public TvShowListApi(Context context) {
        super(context);
    }

    @Override
    protected Void doInBackground(String... urls) {
        fetchTitlesFromCollection();

        for (Map.Entry<String, List<IEpisodeModel>> collection : collections.entrySet()) {
            Log.d(TAG, "Found collection: " + collection.getKey());
            ContentManager.cache().addCollection(collection.getKey(), collection.getValue());
        }

        for (String cat : ContentManager.CATEGORIES.keySet()) {
            fetchTitlesInCategory(cat);
        }
        Log.d(TAG, "Found " + episodes.size() + " episodes from category query");
        fetchTitlesFromIndex();
        ContentManager.cache().putShows(shows);
        ContentManager.cache().addEpisodes(episodes.values());
        ContentManager.cache().setDictionary(buildWordsFromShows(shows));
        success = true;
        return null;
    }

    private void fetchTitlesFromCollection() {
        String response = fetchUrl(getHomeUrl(), CACHE_EXPIRY);
        JSONObject data = parseJSON(response);
        List<IEpisodeModel> titles = getEpisodesFromData(data, true);
        for (IEpisodeModel ep : titles) {
            episodes.put(ep.getHref(), ep);
        }
    }

    private void fetchTitlesFromIndex() {
        String response = fetchUrl(getIndexUrl(), CACHE_EXPIRY);
        JSONObject data = parseJSON(response);
        List<IEpisodeModel> titles = getEpisodesFromData(data, false);
        Log.d(TAG, "Found " + titles.size() + " episode from index query");
        for (IEpisodeModel title : titles) {
            if (episodes.containsKey(title.getHref())) {
                title.setCategories(episodes.get(title.getHref()).getCategories());
            } else {
                episodes.put(title.getHref(), title);
            }
            shows.add(title);
        }
    }

    private void fetchTitlesInCategory(String cat) {
        String response = fetchUrl(getCategoryUrl(cat), CACHE_EXPIRY);
        JSONObject data = parseJSON(response);
        List<IEpisodeModel> titles = getEpisodesFromData(data, false);
        for (IEpisodeModel title : titles) {
            if (episodes.containsKey(title.getHref())) {
                title = episodes.get(title.getHref());
            }
            title.addCategory(cat);
            episodes.put(title.getHref(), title);
        }
    }

    private List<IEpisodeModel> getEpisodesFromData(JSONObject data, boolean addToCollection) {
        List<IEpisodeModel> titles = new ArrayList<>();
        if (data != null) {
            Iterator<String> keys = data.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    if (data.get(key) instanceof JSONArray) {
                        JSONArray groups = data.getJSONArray(key);
                        titles.addAll(getEpisodesFromList(groups, addToCollection));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return titles;
    }

    private List<IEpisodeModel> getEpisodesFromList(JSONArray groups, boolean addToCollection) {
        List<IEpisodeModel> titles = new ArrayList<>();
        for (int i = 0, k = groups.length(); i < k; i++) {
            try {
                if (groups.get(i) instanceof JSONObject) {
                    JSONObject group = groups.getJSONObject(i);
                    List<IEpisodeModel> episodes = new ArrayList<>();
                    if (group.has("episodes") && group.get("episodes") instanceof JSONArray) {
                        JSONArray data = group.getJSONArray("episodes");
                        for (int j = 0, m = data.length(); j < m; j++) {
                            episodes.add(EpisodeModel.create(data.getJSONObject(j)));
                        }
                    }
                    if (episodes.size() > 0 && addToCollection && group.has("title") && group.get("title") instanceof String) {
                        collections.put(group.getString("title"), episodes);
                    }
                    titles.addAll(episodes);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return titles;
    }

    private Uri getCategoryUrl(String cat) {
        return buildApiUrl("category/" + cat);
    }

    private Uri getIndexUrl() {
        String fields[] = {"seriesTitle", "href", "format", "formatBgColour", "formatTextColour", "channel", "pubDate", "thumbnail",
                "livestream", "episodeHouseNumber", "categories", "title", "duration", "label", "rating", "episodeCount"};
        Map<String, String> params = ImmutableMap.of("fields", TextUtils.join(",", fields));
        return buildApiUrl("index", params);
    }

    private Uri getHomeUrl() {
        String fields[] = {"seriesTitle", "href", "format", "formatBgColour", "formatTextColour", "channel", "pubDate", "thumbnail",
                "livestream", "episodeHouseNumber", "categories", "title", "duration", "label", "rating"};
        Map<String, String> params = ImmutableMap.of("fields", TextUtils.join(",", fields));
        return buildApiUrl("home", params);
    }

    protected void onPreExecute() {
        ContentManager.getInstance().broadcastChange(ContentManager.CONTENT_SHOW_LIST_START);
    }

    protected void onPostExecute(Void v) {
        if (success) {
            ContentManager.getInstance().broadcastChange(ContentManager.CONTENT_SHOW_LIST_DONE);
        } else {
            ContentManager.getInstance().broadcastChange(ContentManager.CONTENT_SHOW_LIST_ERROR);
        }
        episodes.clear();
        shows.clear();
    }
}
