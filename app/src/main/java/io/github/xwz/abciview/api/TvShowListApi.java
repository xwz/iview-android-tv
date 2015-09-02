package io.github.xwz.abciview.api;

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

import io.github.xwz.abciview.ImmutableMap;
import io.github.xwz.abciview.content.ContentManager;
import io.github.xwz.abciview.models.EpisodeModel;
import io.github.xwz.abciview.trie.RadixTree;

public class TvShowListApi extends IViewApi {
    private static final String TAG = "TvShowListApi";
    private static final int CACHE_EXPIRY = 600; // 10 mins

    private final Map<String, EpisodeModel> episodes = new HashMap<>();
    private final List<EpisodeModel> shows = new ArrayList<>();
    private LinkedHashMap<String, List<EpisodeModel>> collections = new LinkedHashMap<>();
    private boolean success = false;

    public TvShowListApi(Context context) {
        super(context);
    }

    @Override
    protected Void doInBackground(String... urls) {
        fetchTitlesFromCollection();

        for (Map.Entry<String, List<EpisodeModel>> collection : collections.entrySet()) {
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
        ContentManager.cache().setDictionary(buildWordsFromShows());
        success = true;
        return null;
    }

    private void fetchTitlesFromCollection() {
        String response = fetchUrl(getHomeUrl(), CACHE_EXPIRY);
        JSONObject data = parseJSON(response);
        List<EpisodeModel> titles = getEpisodesFromData(data, true);
        for (EpisodeModel ep : titles) {
            episodes.put(ep.getHref(), ep);
        }
    }

    private RadixTree<String> buildWordsFromShows() {
        RadixTree<String> dict = new RadixTree<>();
        for (EpisodeModel ep : shows) {
            dict.putAll(getWords(ep));
        }
        Log.d(TAG, "dict:" + dict.size());
        return dict;
    }

    private Map<String, String> getWords(EpisodeModel episode) {
        Map<String, String> words = new HashMap<>();
        if (episode.getSeriesTitle() != null) {
            words.putAll(splitWords(episode.getSeriesTitle(), episode));
        }
        if (episode.getTitle() != null) {
            words.putAll(splitWords(episode.getTitle(), episode));
        }
        return words;
    }

    private Map<String, String> splitWords(String s, EpisodeModel episode) {
        String[] words = s.split("\\s+");
        Map<String, String> result = new HashMap<>();
        for (String w : words) {
            String word = w.replaceAll("[^\\w]", "");
            if (word.length() >= 3) {
                result.put(word.toLowerCase(), word);
            }
        }
        return result;
    }

    private void fetchTitlesFromIndex() {
        String response = fetchUrl(getIndexUrl(), CACHE_EXPIRY);
        JSONObject data = parseJSON(response);
        List<EpisodeModel> titles = getEpisodesFromData(data, false);
        Log.d(TAG, "Found " + titles.size() + " episode from index query");
        for (EpisodeModel title : titles) {
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
        List<EpisodeModel> titles = getEpisodesFromData(data, false);
        for (EpisodeModel title : titles) {
            if (episodes.containsKey(title.getHref())) {
                title = episodes.get(title.getHref());
            }
            title.addCategory(cat);
            episodes.put(title.getHref(), title);
        }
    }

    private List<EpisodeModel> getEpisodesFromData(JSONObject data, boolean addToCollection) {
        List<EpisodeModel> titles = new ArrayList<>();
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

    private List<EpisodeModel> getEpisodesFromList(JSONArray groups, boolean addToCollection) {
        List<EpisodeModel> titles = new ArrayList<>();
        for (int i = 0, k = groups.length(); i < k; i++) {
            try {
                if (groups.get(i) instanceof JSONObject) {
                    JSONObject group = groups.getJSONObject(i);
                    List<EpisodeModel> episodes = new ArrayList<>();
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
