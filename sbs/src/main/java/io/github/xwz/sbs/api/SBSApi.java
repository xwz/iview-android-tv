package io.github.xwz.sbs.api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.xwz.base.ImmutableMap;
import io.github.xwz.base.Utils;
import io.github.xwz.base.api.IEpisodeModel;
import io.github.xwz.base.content.IContentManager;
import io.github.xwz.sbs.content.ContentManager;

public class SBSApi extends SBSApiBase {
    private static final String TAG = "SBSApi";

    private static final int CACHE_EXPIRY = 1800; // 30 mins
    private static final int ITEMS_PER_PAGE = 250;

    private int page = 0;
    private int progress = 0;

    private final List<IEpisodeModel> episodes = new ArrayList<>();
    private HashMap<String, List<IEpisodeModel>> showList = new HashMap<>();
    private boolean success = false;

    private static final String[] PROGRESS = new String[]{
            "Loading SBS1...", "Loading SBS2...", "Loading Action Adventures...", "Loading Animations...", "Loading Classics...",
            "Loading Comedy...", "Loading Documentaries...", "Loading Drama...", "Loading Fantasy...", "Loading History...",
            "Loading Horror...", "Loading Martial Arts...", "Loading Science Fictions...", "Loading Food...", "Loading Sports...",
    };

    public SBSApi(Context context) {
        super(context);
    }

    @Override
    protected Void doInBackground(String... params) {
        while (getLastEntryCount() == 0 || getLastEntryCount() == ITEMS_PER_PAGE) {
            updateProgress();
            fetchAllTitles(page++);
        }

        // Build a list of shows
        List<IEpisodeModel> shows = new ArrayList<>();
        for (List<IEpisodeModel> eps : showList.values()) {
            IEpisodeModel show = eps.get(0);

            // calculate other episodes
            for (IEpisodeModel ep : eps) {
                List<IEpisodeModel> others = new ArrayList<>(eps);
                others.remove(ep);
                Map<String, List<IEpisodeModel>> more = new HashMap<>();
                more.put(IContentManager.OTHER_EPISODES, others);
                ep.setOtherEpisodes(more);
                ep.setHasExtra(others.size() > 0);
            }
            shows.add(show);
        }

        // build a list of show collections by category
        Map<String, List<IEpisodeModel>> collections = new HashMap<>();
        for (IEpisodeModel show : shows) {
            for (String cat : show.getCategories()) {
                if (!collections.containsKey(cat)) {
                    collections.put(cat, new ArrayList<IEpisodeModel>());
                }
                collections.get(cat).add(show);
            }
        }

        for (Map.Entry<String, Uri> entry : getFeaturedUrls().entrySet()) {
            updateProgress("Loading " + Utils.stripCategory(entry.getKey()) + "...");
            List<IEpisodeModel> features = fetchContent(entry.getValue(), CACHE_EXPIRY);
            collections.put(entry.getKey(), features);
        }

        // sort the collection names
        List<String> keys = new ArrayList<>();
        keys.addAll(collections.keySet());
        Collections.sort(keys);

        // add collections by name
        for (String key : keys) {
            List<IEpisodeModel> collection = collections.get(key);
            Log.d(TAG, "Found collection: " + key + " = " + collection.size());
            String[] parts = key.split("/");
            String name = parts.length > 1 ? parts[1] : key;
            if (key.contains("Film/")) {
                name = key;
            }
            ContentManager.cache().addCollection(name, collection);
        }
        updateProgress();
        ContentManager.cache().putShows(shows);
        ContentManager.cache().addEpisodes(episodes);
        ContentManager.cache().setDictionary(buildWordsFromShows(shows));
        updateProgress();

        success = true;
        return null;
    }

    private void updateProgress() {
        updateProgress(PROGRESS[progress++ % PROGRESS.length]);
    }

    private void updateProgress(String str) {
        ContentManager.getInstance().broadcastChange(ContentManager.CONTENT_SHOW_LIST_PROGRESS, str);
    }

    protected Map<String, Uri> getFeaturedUrls() {
        return ImmutableMap.of(
                "AAA1/Featured", getFeaturedUrl(),
                "AAA2/What you missed last night", getLastNightUrl(),
                "AAA3/Trending now", getTrendingUrl(),
                "AAA4/Popular movies", getPopularFilms()
        );
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
        showList.clear();
    }

    private void fetchAllTitles(int page) {
        List<IEpisodeModel> all = fetchContent(getIndexUrl(page), CACHE_EXPIRY);
        for (IEpisodeModel ep : all) {
            addToShowList(ep.getSeriesTitle(), ep);
            episodes.add(ep);
        }
    }

    private void addToShowList(String title, IEpisodeModel ep) {
        if (!showList.containsKey(title)) {
            showList.put(title, new ArrayList<IEpisodeModel>());
        }
        showList.get(title).add(ep);
    }

    private Uri getIndexUrl(int page) {
        String range = String.format("%d-%d", page * ITEMS_PER_PAGE + 1, (page + 1) * ITEMS_PER_PAGE);
        Map<String, String> params = ImmutableMap.of("form", "json", "range", range);
        return buildApiUrl(params);
    }

    private Uri getFeaturedUrl() {
        String range = String.format("%d-%d", 1, 30);
        Map<String, String> params = ImmutableMap.of("form", "json", "range", range, "sort", "pubDate|asc", "byCategories", "!Film");
        return buildFeaturedUrl(params);
    }

    private Uri getLastNightUrl() {
        String range = String.format("%d-%d", 1, 30);
        Calendar yesterday = new GregorianCalendar();
        yesterday.setTime(new Date((new Date()).getTime() - 86400000));
        yesterday.set(Calendar.MILLISECOND, 0);
        yesterday.set(Calendar.SECOND, 0);
        yesterday.set(Calendar.MINUTE, 30);
        yesterday.set(Calendar.HOUR_OF_DAY, 17);
        long start = yesterday.getTimeInMillis();
        yesterday.set(Calendar.HOUR_OF_DAY, 23);
        long end = yesterday.getTimeInMillis();
        String date = String.format("%d~%d", start, end);
        Map<String, String> params = ImmutableMap.of("form", "json", "byPubDate", date, "sort", "pubDate|desc", "range", range);
        return buildApiUrl(params);
    }

    private Uri getTrendingUrl() {
        return buildTrendingUrl(new HashMap<String, String>());
    }

    private Uri getPopularFilms() {
        Map<String, String> params = ImmutableMap.of("section", "film");
        return buildTrendingUrl(params);
    }
}
