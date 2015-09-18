package io.github.xwz.iview.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.github.xwz.base.api.EpisodeBaseModel;

public class EpisodeModel extends EpisodeBaseModel {
    private static final String TAG = "EpisodeModel";

    private boolean extras = false;
    private String stream;

    public static EpisodeModel create(JSONObject data) {
        EpisodeModel ep = new EpisodeModel();
        ep.set(data);
        return ep;
    }

    @SuppressWarnings({"unchecked", "TypeParameterExplicitlyExtendsObject"})
    private static <T extends Object> T get(JSONObject data, String key, T fallback) {
        T value = null;
        try {
            value = data != null && key != null ? (T) data.get(key) : null;
        } catch (JSONException e) {
            //Log.d(TAG, "No string value for: " + key);
        }
        return value != null ? value : fallback;
    }

    private void set(JSONObject data) {
        setSeriesTitle(get(data, "seriesTitle", getSeriesTitle()));
        setHref(get(data, "href", getHref()));
        setChannel(get(data, "channel", getChannel()));
        setThumbnail(get(data, "thumbnail", getThumbnail()));
        setEpisodeHouseNumber(get(data, "episodeHouseNumber", getEpisodeHouseNumber()));
        setTitle(get(data, "title", getTitle()));
        setDuration(getInt(data, "duration", getDuration()));
        setRating(get(data, "rating", getRating()));
        setEpisodeCount(getInt(data, "episodeCount", getEpisodeCount()));
        setDescription(get(data, "description", getDescription()));
        setRelated(get(data, "related", getRelated()));
        String date = get(data, "pubDate", null);
        setPubDate(parseDate(date));

        try {
            stream = getStream(data);
        } catch (JSONException ignored) {
        }

        if (stream != null) {
            extras = true;
        }
    }

    public String getStream() {
        return stream;
    }

    private long parseDate(String d) {
        if (d != null) {
            // 2015-09-17 07:02:00
            DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date date = f.parse(d);
                return date.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public void merge(EpisodeModel ep) {
        super.merge(ep);
        this.stream = ep.stream == null ? this.stream : ep.stream;
        this.extras = this.stream != null;
    }

    private String getStream(JSONObject data) throws JSONException {
        if (data.has("playlist") && data.get("playlist") instanceof JSONArray) {
            JSONArray playlists = data.getJSONArray("playlist");
            for (int i = 0, k = playlists.length(); i < k; i++) {
                if (playlists.get(i) instanceof JSONObject) {
                    JSONObject playlist = (JSONObject) playlists.get(i);
                    String type = get(playlist, "type", "");
                    if ("program".equals(type)) {
                        return get(playlist, "hls-high", null);
                    }
                }
            }
        }
        return null;
    }

    private static int getInt(JSONObject data, String key, int fallback) {
        if (data != null && key != null && data.has(key)) {
            try {
                return data.getInt(key);
            } catch (JSONException e) {
                //Log.d(TAG, "No int value for: " + key);
            }
        }
        return fallback;
    }

    public boolean hasExtras() {
        return extras;
    }
}
