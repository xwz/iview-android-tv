package io.github.xwz.base.models;

import java.util.List;
import java.util.Map;

public class CategoryModel implements IEpisodeModel {
    private String category;
    private int shows;

    public CategoryModel(String cat) {
        category = cat;
    }

    public void setEpisodeCount(int count) {
        shows = count;
    }

    @Override
    public void merge(IEpisodeModel ep) {

    }

    @Override
    public boolean matches(String query) {
        return false;
    }

    @Override
    public void setOtherEpisodes(Map<String, List<IEpisodeModel>> more) {

    }

    @Override
    public Map<String, List<IEpisodeModel>> getOtherEpisodes() {
        return null;
    }

    @Override
    public List<String> getOtherEpisodeUrls(String cat) {
        return null;
    }

    @Override
    public String getDurationText() {
        return null;
    }

    @Override
    public void addCategory(String cat) {

    }

    @Override
    public void setCategories(List<String> cats) {

    }

    @Override
    public String toString() {
        return category;
    }

    @Override
    public String getSeriesTitle() {
        return null;
    }

    @Override
    public String getHref() {
        return null;
    }

    @Override
    public String getChannel() {
        return null;
    }

    @Override
    public String getThumbnail() {
        return null;
    }

    @Override
    public String getLivestream() {
        return null;
    }

    @Override
    public String getEpisodeHouseNumber() {
        return null;
    }

    @Override
    public List<String> getCategories() {
        return null;
    }

    @Override
    public String getTitle() {
        return category;
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public String getRating() {
        return null;
    }

    @Override
    public int getEpisodeCount() {
        return shows;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getRelated() {
        return null;
    }

    @Override
    public String getAvailability() {
        return null;
    }

    @Override
    public String getStream() {
        return null;
    }

    @Override
    public String getCaptions() {
        return null;
    }

    @Override
    public String getShare() {
        return null;
    }

    @Override
    public boolean hasExtras() {
        return false;
    }

    @Override
    public boolean hasOtherEpisodes() {
        return false;
    }
}
