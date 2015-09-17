package io.github.xwz.iview.api;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.xwz.base.api.EpisodeBaseModel;
import io.github.xwz.iview.content.ContentManager;

public class EpisodeDetailsApi extends IViewApi {
    private static final String TAG = "EpisodeDetailsApi";
    private static final int CACHE_EXPIRY = 60; // 1 min
    private boolean success = false;
    private final String id;

    public EpisodeDetailsApi(Context context, String id) {
        super(context);
        this.id = id;
    }

    @Override
    protected Void doInBackground(String... urls) {
        if (urls.length > 0) {
            updateEpisode(urls[0]);
        }
        return null;
    }

    boolean updateEpisode(String url) {
        EpisodeModel ep = fetchEpisodeDetails(url);
        if (ep != null) {
            if (ep.getRelated() != null) {
                if (fetchRelatedEpisode(ep, ep.getRelated())) {
                    EpisodeModel existing = (EpisodeModel)ContentManager.cache().updateEpisode(ep);
                    existing.merge(ep);
                    success = true;
                }
            }
        }
        if (!success) {
            Log.w(TAG, "No episode detail from cache or network");
            if (ContentManager.cache().getEpisode(url) != null) {
                success = true;
            }
        }
        return success;
    }

    private EpisodeModel fetchEpisodeDetails(String url) {
        Log.d(TAG, "Fetching episode details: " + url);
        String response = fetchUrl(buildApiUrl(url), CACHE_EXPIRY);
        JSONObject data = parseJSON(response);
        if (data != null) {
            return EpisodeModel.create(data);
        }
        return null;
    }

    private boolean fetchRelatedEpisode(EpisodeModel ep, String related) {
        String response = fetchUrl(buildApiUrl(related), CACHE_EXPIRY);
        JSONObject data = parseJSON(response);
        try {
            if (data != null && data.has("index") && data.get("index") instanceof JSONArray) {
                Map<String, List<EpisodeBaseModel>> others = getEpisodesFromList(data.getJSONArray("index"));
                ep.setOtherEpisodes(others);
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Map<String, List<EpisodeBaseModel>> getEpisodesFromList(JSONArray groups) {
        Map<String, List<EpisodeBaseModel>> related = new LinkedHashMap<>();
        for (int i = 0, k = groups.length(); i < k; i++) {
            try {
                if (groups.get(i) instanceof JSONObject) {
                    JSONObject group = groups.getJSONObject(i);
                    if (group.has("episodes") && group.get("episodes") instanceof JSONArray) {
                        List<EpisodeBaseModel> titles = new ArrayList<>();
                        JSONArray episodes = group.getJSONArray("episodes");
                        for (int j = 0, m = episodes.length(); j < m; j++) {
                            titles.add(EpisodeModel.create(episodes.getJSONObject(j)));
                        }
                        String type = get(group, "title", "More");
                        if (type.contains("Other Episode")) {
                            type = ContentManager.OTHER_EPISODES;
                        }
                        ContentManager.cache().addEpisodes(titles);
                        related.put(type, titles);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return related;
    }

    protected void onPreExecute() {
        ContentManager.getInstance().broadcastChange(ContentManager.CONTENT_EPISODE_START, id);
    }

    protected void onPostExecute(Void v) {
        if (success) {
            ContentManager.getInstance().broadcastChange(ContentManager.CONTENT_EPISODE_DONE, id);
        } else {
            ContentManager.getInstance().broadcastChange(ContentManager.CONTENT_EPISODE_ERROR, id);
        }
    }
}
