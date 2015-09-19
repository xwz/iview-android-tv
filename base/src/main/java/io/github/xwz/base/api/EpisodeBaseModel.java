package io.github.xwz.base.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.xwz.base.Utils;
import io.github.xwz.base.content.ContentManagerBase;

@Table(databaseName = ContentDatabase.NAME)
public class EpisodeBaseModel extends BaseModel implements Serializable {

    private static final String TAG = "EpisodeBaseModel";

    @Column
    @PrimaryKey(autoincrement = true)
    public long DATA_ID;

    @Column
    public String DATA_TYPE;

    @Column
    public String DATA_COLLECTION_KEY;

    @Column
    public int DATA_COLLECTION_INDEX;

    @Column
    private String href;

    @Column
    private String seriesTitle;

    @Column
    private String channel;

    @Column
    private String thumbnail;

    @Column
    private String episodeHouseNumber;

    private Set<String> categories = new HashSet<>();

    @Column
    public String categoriesSerialized;

    @Column
    private String title;

    @Column
    private int duration;

    @Column
    private String rating;

    @Column
    private int episodeCount;

    @Column
    private String description;

    @Column
    private String cover;

    @Column
    private boolean isFilm = false;

    @Column
    private String related;

    @Column
    private long expiry;

    @Column
    private long pubDate;

    private long resumePosition;
    private boolean recent;

    private Map<String, List<EpisodeBaseModel>> others = new HashMap<>();

    public List<String> getCategories() {
        return new ArrayList<>(categories);
    }

    public void save() {
        updateCategoriesSerialized();
        super.save();
    }

    public void insert() {
        updateCategoriesSerialized();
        super.insert();
    }

    public void update() {
        updateCategoriesSerialized();
        super.update();
    }

    void unserialize() {
        if (categoriesSerialized != null) {
            JSONArray arr = parseArray(categoriesSerialized);
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    try {
                        addCategory(arr.getString(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private JSONArray parseArray(String content) {
        if (content != null && content.contains("[") && content.contains("]")) {
            try {
                return new JSONArray(content);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void addCategory(String cat) {
        categories.add(cat);
    }

    public void setCategories(List<String> cats) {
        categories = new HashSet<>(cats);
    }

    private void updateCategoriesSerialized() {
        Gson gson = new GsonBuilder().create();
        categoriesSerialized = gson.toJson(categories);
    }

    public String getDurationText() {
        if (getRating() != null) {
            return getRating() + ", " + Utils.formatMillis(getDuration() * 1000);
        } else {
            return Utils.formatMillis(getDuration() * 1000);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof EpisodeBaseModel))
            return false;
        EpisodeBaseModel other = (EpisodeBaseModel) o;
        return other.getHref().equals(this.getHref());
    }

    public void merge(EpisodeBaseModel ep) {
        this.href = ep.href == null ? this.href : ep.href;
        this.seriesTitle = ep.seriesTitle == null ? this.seriesTitle : ep.seriesTitle;
        this.channel = ep.channel == null ? this.channel : ep.channel;
        this.thumbnail = ep.thumbnail == null ? this.thumbnail : ep.thumbnail;
        this.episodeHouseNumber = ep.episodeHouseNumber == null ? this.episodeHouseNumber : ep.episodeHouseNumber;
        this.categories.addAll(ep.categories);
        this.title = ep.title == null ? this.title : ep.title;
        this.duration = ep.duration;
        this.rating = ep.rating == null ? this.rating : ep.rating;
        this.episodeCount = ep.episodeCount;
        this.description = ep.description == null ? this.description : ep.description;
        this.cover = ep.cover == null ? this.cover : ep.cover;
        this.isFilm = ep.isFilm;
        this.others = ep.others.size() == 0 ? this.others : new LinkedHashMap<>(ep.others);
        this.related = ep.related == null ? this.related : ep.related;
        this.expiry = ep.expiry == 0 ? this.expiry : ep.expiry;
        this.pubDate = ep.pubDate == 0 ? this.pubDate : ep.pubDate;
    }

    protected boolean hasOther(String key) {
        return others.containsKey(key);
    }

    public void setOtherEpisodes(Map<String, List<EpisodeBaseModel>> more) {
        others = more;
    }

    public void setOtherEpisodes(String key, List<EpisodeBaseModel> more) {
        others.put(key, more);
    }

    public Map<String, List<EpisodeBaseModel>> getOtherEpisodes() {
        LinkedHashMap<String, List<EpisodeBaseModel>> all = new LinkedHashMap<>();
        for (Map.Entry<String, List<EpisodeBaseModel>> more : others().entrySet()) {
            List<EpisodeBaseModel> episodes = new ArrayList<>(more.getValue());
            if (ContentManagerBase.OTHER_EPISODES.equals(more.getKey())) {
                Collections.sort(episodes, comparePubDate());
            }
            all.put(more.getKey(), episodes);
        }
        return all;
    }

    protected Map<String, List<EpisodeBaseModel>> others() {
        return others;
    }

    private List<EpisodeBaseModel> getOtherEpisodes(String cat) {
        Map<String, List<EpisodeBaseModel>> all = getOtherEpisodes();
        if (all.containsKey(cat)) {
            return all.get(cat);
        }
        return new ArrayList<>();
    }

    protected Comparator<EpisodeBaseModel> comparePubDate() {
        return new Comparator<EpisodeBaseModel>() {
            @Override
            public int compare(EpisodeBaseModel lhs, EpisodeBaseModel rhs) {
                return (int) (lhs.pubDate - rhs.pubDate);
            }
        };
    }

    protected Comparator<EpisodeBaseModel> compareTitle() {
        return new Comparator<EpisodeBaseModel>() {
            @Override
            public int compare(EpisodeBaseModel lhs, EpisodeBaseModel rhs) {
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        };
    }

    public List<String> getOtherEpisodeUrls(String cat) {
        List<String> urls = new ArrayList<>();
        for (EpisodeBaseModel ep : getOtherEpisodes(cat)) {
            urls.add(ep.getHref());
        }
        return urls;
    }

    public boolean hasOtherEpisodes() {
        return others.size() > 0;
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

    public boolean hasCover() {
        return getIsFilm() && getCover() != null && getCover().length() > 0;
    }

    public String getSeriesTitle() {
        return seriesTitle;
    }

    public void setSeriesTitle(String seriesTitle) {
        this.seriesTitle = seriesTitle;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getEpisodeHouseNumber() {
        return episodeHouseNumber;
    }

    public void setEpisodeHouseNumber(String episodeHouseNumber) {
        this.episodeHouseNumber = episodeHouseNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public int getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        this.episodeCount = episodeCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public boolean getIsFilm() {
        return isFilm;
    }

    public void setIsFilm(boolean film) {
        this.isFilm = film;
    }

    public String getRelated() {
        return related;
    }

    public void setRelated(String related) {
        this.related = related;
    }

    public String toString() {
        return "{" + getHref() + ":'" + getSeriesTitle() + "', '" + getTitle() + "', " + pubDate + "}";
    }

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    public long getResumePosition() {
        return resumePosition;
    }

    public void setResumePosition(long resumePosition) {
        this.resumePosition = resumePosition;
    }

    public int getProgress() {
        return Math.round(100 * (float) getResumePosition() / (getDuration() * 1000));
    }

    public boolean isRecent() {
        return recent;
    }

    public void setRecent(boolean recent) {
        this.recent = recent;
    }

    public long getPubDate() {
        return pubDate;
    }

    public void setPubDate(long pubDate) {
        this.pubDate = pubDate;
    }
}
