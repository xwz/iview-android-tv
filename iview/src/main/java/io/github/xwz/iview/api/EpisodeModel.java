package io.github.xwz.iview.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.xwz.base.Utils;
import io.github.xwz.base.api.IEpisodeModel;

public class EpisodeModel implements IEpisodeModel {
    private static final String TAG = "EpisodeModel";

    private String seriesTitle;
    private String href;
    private String format;
    private String formatBgColour;
    private String formatTextColour;
    private String channel;
    private String pubDate;
    private String thumbnail;
    private String livestream;
    private String episodeHouseNumber;
    private Set<String> categories = new HashSet<>();
    private String title;
    private int duration;
    private String label;
    private String rating;
    private int episodeCount;

    private String description;
    private String related;
    private String availability;
    private String stream;
    private String captions;
    private String share;

    private boolean extras = false;

    private Map<String, List<IEpisodeModel>> others = new HashMap<>();

    public static IEpisodeModel create(JSONObject data) {
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

    public static String getTAG() {
        return TAG;
    }

    private void set(JSONObject data) {
        seriesTitle = get(data, "seriesTitle", getSeriesTitle());
        href = get(data, "href", getHref());
        format = get(data, "format", getFormat());
        formatBgColour = get(data, "formatBgColour", getFormatBgColour());
        formatTextColour = get(data, "formatTextColour", getFormatTextColour());
        channel = get(data, "channel", getChannel());
        pubDate = get(data, "pubDate", getPubDate());
        thumbnail = get(data, "thumbnail", getThumbnail());
        livestream = get(data, "livestream", getLivestream());
        episodeHouseNumber = get(data, "episodeHouseNumber", getEpisodeHouseNumber());
        title = get(data, "title", getTitle());
        duration = getInt(data, "duration", getDuration());
        label = get(data, "label", getLabel());
        rating = get(data, "rating", getRating());
        episodeCount = getInt(data, "episodeCount", getEpisodeCount());

        description = get(data, "description", getDescription());
        related = get(data, "related", getRelated());
        availability = get(data, "availability", getAvailability());
        captions = get(data, "captions", getCaptions());
        share = get(data, "share", getShare());

        try {
            stream = getStream(data);
        } catch (JSONException ignored) {
        }

        if (stream != null) {
            extras = true;
        }
    }

    private void merge(EpisodeModel ep) {
        this.seriesTitle = ep.seriesTitle == null ? this.seriesTitle : ep.seriesTitle;
        this.href = ep.href == null ? this.href : ep.href;
        this.format = ep.format == null ? this.format : ep.format;
        this.formatBgColour = ep.formatBgColour == null ? this.formatBgColour : ep.formatBgColour;
        this.formatTextColour = ep.formatTextColour == null ? this.formatTextColour : ep.formatTextColour;
        this.channel = ep.channel == null ? this.channel : ep.channel;
        this.pubDate = ep.pubDate == null ? this.pubDate : ep.pubDate;
        this.thumbnail = ep.thumbnail == null ? this.thumbnail : ep.thumbnail;
        this.livestream = ep.livestream == null ? this.livestream : ep.livestream;
        this.episodeHouseNumber = ep.episodeHouseNumber == null ? this.episodeHouseNumber : ep.episodeHouseNumber;
        this.title = ep.title == null ? this.title : ep.title;
        this.duration = ep.duration;
        this.label = ep.label == null ? this.label : ep.label;
        this.rating = ep.rating == null ? this.rating : ep.rating;
        this.episodeCount = ep.episodeCount;
        this.description = ep.description == null ? this.description : ep.description;
        this.related = ep.related == null ? this.related : ep.related;
        this.availability = ep.availability == null ? this.availability : ep.availability;
        this.stream = ep.stream == null ? this.stream : ep.stream;
        this.captions = ep.captions == null ? this.captions : ep.captions;
        this.share = ep.share == null ? this.share : ep.share;

        this.categories.addAll(ep.categories);
        this.others = ep.others.size() == 0 ? this.others : new LinkedHashMap<>(ep.others);

        this.extras = this.stream != null;
    }

    public void setEpisodeCount(int count) {
        episodeCount = count;
    }

    @Override
    public void setHasExtra(boolean extra) {
        extras = extra;
    }

    @Override
    public void merge(IEpisodeModel ep) {
        merge((EpisodeModel)ep);
    }

    public void setOtherEpisodes(Map<String, List<IEpisodeModel>> more) {
        others = more;
    }

    public Map<String, List<IEpisodeModel>> getOtherEpisodes() {
        return new LinkedHashMap<>(others);
    }

    private List<IEpisodeModel> getOtherEpisodes(String cat) {
        for (Map.Entry<String, List<IEpisodeModel>> episodes : getOtherEpisodes().entrySet()) {
            if (episodes.getKey().equals(cat)) {
                return episodes.getValue();
            }
        }
        return new ArrayList<>();
    }

    public List<String> getOtherEpisodeUrls(String cat) {
        List<String> urls = new ArrayList<>();
        for (IEpisodeModel ep : getOtherEpisodes(cat)) {
            urls.add(ep.getHref());
        }
        return urls;
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

    public boolean matches(String query) {
        boolean found = false;
        if (getSeriesTitle() != null) {
            found = found || getSeriesTitle().toLowerCase().contains(query);
        }
        if (getTitle() != null) {
            found = found || getTitle().toLowerCase().contains(query);
        }
        return found;
    }

    private static boolean getBoolean(JSONObject data, String key, boolean fallback) {
        if (data != null && key != null && data.has(key)) {
            try {
                return data.getBoolean(key);
            } catch (JSONException e) {
                //Log.d(TAG, "No boolean value for: " + key);
            }
        }
        return fallback;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof EpisodeModel))
            return false;
        EpisodeModel other = (EpisodeModel) o;
        return other.getHref().equals(this.getHref());
    }

    public String getDurationText() {
        if (getRating() != null) {
            return getRating() + ", " + Utils.formatMillis(getDuration() * 1000);
        } else {
            return Utils.formatMillis(getDuration() * 1000);
        }
    }

    public void addCategory(String cat) {
        categories.add(cat);
    }

    public void setCategories(List<String> cats) {
        categories = new HashSet<>(cats);
    }

    public String toString() {
        return getHref() + ": '" + getSeriesTitle() + "' - '" + getTitle() + "'";
    }

    public String getSeriesTitle() {
        return seriesTitle;
    }

    public String getHref() {
        return href;
    }

    public String getFormat() {
        return format;
    }

    public String getFormatBgColour() {
        return formatBgColour;
    }

    public String getFormatTextColour() {
        return formatTextColour;
    }

    public String getChannel() {
        return channel;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getLivestream() {
        return livestream;
    }

    public String getEpisodeHouseNumber() {
        return episodeHouseNumber;
    }

    public List<String> getCategories() {
        return new ArrayList<>(categories);
    }

    public String getTitle() {
        return title;
    }

    public int getDuration() {
        return duration;
    }

    public String getLabel() {
        return label;
    }

    public String getRating() {
        return rating;
    }

    public int getEpisodeCount() {
        return episodeCount;
    }

    public String getDescription() {
        return description;
    }

    public String getRelated() {
        return related;
    }

    public String getAvailability() {
        return availability;
    }

    public String getStream() {
        return stream;
    }

    public String getCaptions() {
        return captions;
    }

    public String getShare() {
        return share;
    }

    public boolean hasExtras() {
        return extras;
    }

    public boolean hasOtherEpisodes() {
        return others.size() > 0;
    }

    @Override
    public boolean isFilm() {
        return false;
    }

    @Override
    public String getCover() {
        return null;
    }
}
