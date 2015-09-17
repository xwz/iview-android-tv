package io.github.xwz.base.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.xwz.base.content.ContentDatabase;

@Table(databaseName = ContentDatabase.NAME)
public class EpisodeBaseModel extends BaseModel {

    @Column
    @PrimaryKey
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

    private void updateCategoriesSerialized() {
        Gson gson = new GsonBuilder().create();
        categoriesSerialized = gson.toJson(categories);
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
}
