package io.github.xwz.base.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface IEpisodeModel extends Serializable {

    void merge(IEpisodeModel ep);

    boolean matches(String query);

    void setOtherEpisodes(Map<String, List<IEpisodeModel>> more);

    void setEpisodeCount(int count);

    void setHasExtra(boolean extra);

    Map<String, List<IEpisodeModel>> getOtherEpisodes();

    List<String> getOtherEpisodeUrls(String cat);

    String getDurationText();

    void addCategory(String cat);

    void setCategories(List<String> cats);

    String toString();

    String getSeriesTitle();

    String getHref();

    String getChannel();

    String getThumbnail();

    String getLivestream();

    String getEpisodeHouseNumber();

    List<String> getCategories();

    String getTitle();

    int getDuration();

    String getRating();

    int getEpisodeCount();

    String getDescription();

    String getRelated();

    String getAvailability();

    String getStream();

    String getCaptions();

    String getShare();

    boolean hasExtras();

    boolean hasOtherEpisodes();
}
