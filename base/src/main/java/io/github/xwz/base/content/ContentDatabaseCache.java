package io.github.xwz.base.content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import io.github.xwz.base.api.EpisodeBaseModel;

public class ContentDatabaseCache {
    private static final String TYPE_EPISODES = "EPISODES";
    private static final String TYPE_COLLECTIONS = "COLLECTIONS";
    private static final String TYPE_SHOWS = "SHOWS";


    public List<EpisodeBaseModel> getAllShows() {
        return new ArrayList<>();
    }

    public boolean hasShows() {
        return false;
    }

    public void putShows(Collection<EpisodeBaseModel> shows) {
    }

    public void putCollections(LinkedHashMap<String, List<EpisodeBaseModel>> shows) {
    }

    public LinkedHashMap<String, List<EpisodeBaseModel>> getCollections() {
        return new LinkedHashMap<>();
    }

    public void putEpisodes(Collection<EpisodeBaseModel> episodes) {

    }

    public List<EpisodeBaseModel> getEpisodes() {
        return new ArrayList<>();
    }
}
