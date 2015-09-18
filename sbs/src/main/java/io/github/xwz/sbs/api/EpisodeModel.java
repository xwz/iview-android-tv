package io.github.xwz.sbs.api;

import io.github.xwz.base.api.EpisodeBaseModel;
import io.github.xwz.base.content.ContentManagerBase;

public class EpisodeModel extends EpisodeBaseModel {
    private static final String TAG = "EpisodeModel";
    private boolean fetchedRelated = false;
    private boolean extras = false;

    public static EpisodeModel create(SBSApi.Entry data) {
        EpisodeModel ep = new EpisodeModel();
        ep.set(data);
        return ep;
    }

    private void set(SBSApi.Entry data) {
        setSeriesTitle(data.series);
        setHref(data.id);

        setChannel(data.getChannel());
        setThumbnail(data.getThumbnail());
        setEpisodeHouseNumber(data.guid);
        setTitle(data.title);
        setDuration(data.getDuration());
        setRating(data.getRating());
        setIsFilm(data.isFilm());
        setCover(data.getCover());
        setCategories(data.getCategories());
        setDescription(data.synopsis);
        setExpiry(data.expiry);
    }

    public void setHasFetchedRelated(boolean fetched) {
        fetchedRelated = fetched;
    }

    public void setHasExtra(boolean extras) {
        this.extras = extras;
    }

    public boolean hasExtras() {
        return extras;
    }

    public boolean hasOtherEpisodes() {
        return fetchedRelated || hasOther(ContentManagerBase.MORE_LIKE_THIS);
    }
}
