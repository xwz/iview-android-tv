package io.github.xwz.sbs.api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.xwz.base.ImmutableMap;
import io.github.xwz.base.models.IEpisodeModel;
import io.github.xwz.sbs.content.ContentManager;

public class SBSApi extends SBSApiBase {
    private static final String TAG = "SBSApi";

    private static final int CACHE_EXPIRY = 600; // 10 mins
    private static final int ITEMS_PER_PAGE = 1000;

    private int lastEntryCount = 0;
    private int page = 0;

    private final List<IEpisodeModel> episodes = new ArrayList<>();
    private final LinkedHashMap<String, IEpisodeModel> shows = new LinkedHashMap<>();
    private HashMap<String, List<IEpisodeModel>> collections = new HashMap<>();
    private boolean success = false;

    public SBSApi(Context context) {
        super(context);
    }

    @Override
    protected Void doInBackground(String... params) {
        while (lastEntryCount == 0 || lastEntryCount == ITEMS_PER_PAGE) {
            fetchAllTitles(page++);
        }

        List<String> keys = new ArrayList<>();
        keys.addAll(collections.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            List<IEpisodeModel> collection = collections.get(key);
            Log.d(TAG, "Found collection: " + key + " = " + collection.size());
            ContentManager.cache().addCollection(key, collection);
        }

        ContentManager.cache().putShows(shows.values());
        ContentManager.cache().addEpisodes(episodes);
        ContentManager.cache().setDictionary(buildWordsFromShows(shows.values()));

        success = true;
        return null;
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
        shows.clear();
    }

    private void fetchAllTitles(int page) {
        InputStream response = fetchStream(getIndexUrl(page), CACHE_EXPIRY);
        if (response != null) {
            try {
                JsonReader reader = new JsonReader(new InputStreamReader(response, "UTF-8"));
                reader.beginObject();
                while (reader.hasNext()) {
                    JsonToken token = reader.peek();
                    if (token == JsonToken.NAME) {
                        String name = reader.nextName();
                        if (name.equals("entryCount")) {
                            lastEntryCount = reader.nextInt();
                        } else if (name.equals("entries")) {
                            readEntriesArray(reader);
                        } else {
                            reader.skipValue();
                        }
                    }
                }
                reader.endObject();
                reader.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readEntriesArray(JsonReader reader) throws IOException {
        reader.beginArray();
        Gson gson = new GsonBuilder().create();
        while (reader.hasNext()) {
            try {
                Entry entry = gson.fromJson(reader, Entry.class);
                IEpisodeModel ep = EpisodeModel.create(entry);
                episodes.add(ep);
                shows.put(ep.getSeriesTitle(), ep);
                for (String cat : ep.getCategories()) {
                    addToCollection(cat, ep);
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        reader.endArray();
    }

    private void addToCollection(String cat, IEpisodeModel ep) {
        if (!collections.containsKey(cat)) {
            collections.put(cat, new ArrayList<IEpisodeModel>());
        }
        collections.get(cat).add(ep);
    }

    private Uri getIndexUrl(int page) {
        String range = String.format("%d-%d", page * ITEMS_PER_PAGE + 1, (page + 1) * ITEMS_PER_PAGE);
        Map<String, String> params = ImmutableMap.of("format", "json", "range", range);
        return buildApiUrl(params);
    }

    static class Category {
        @SerializedName("media$name")
        private String name;

        @SerializedName("media$scheme")
        private String scheme;

        @SerializedName("media$label")
        private String label;

        public String toString() {
            return name + " : " + scheme + " : " + label;
        }

        public String getName() {
            if (name != null) {
                String[] parts = name.split("/");
                return parts.length > 1 ? parts[1] : name;
            }
            return name;
        }
    }

    static class Thumbnail {
        @SerializedName("plfile$height")
        private int height;

        @SerializedName("plfile$width")
        private int width;

        @SerializedName("plfile$downloadUrl")
        private String url;

        public String toString() {
            return width + "x" + height + ":" + url;
        }
    }

    static class Content {
        @SerializedName("plfile$duration")
        private String duration;

        @SerializedName("plfile$bitrate")
        private int bitrate;

        @SerializedName("plfile$downloadUrl")
        private String url;
    }

    static class Rating {
        private String rating;
    }

    public static class Entry {
        String id;
        String guid;
        String title;
        String author;
        String description;

        @SerializedName("plmedia$defaultThumbnailUrl")
        String thumbnail;

        @SerializedName("pl1$programName")
        String series;

        @SerializedName("pl1$seriesId")
        String seriesId;

        @SerializedName("pl1$shortSynopsis")
        String synopsis;

        @SerializedName("media$categories")
        private List<Category> categories;

        @SerializedName("media$thumbnails")
        private List<Thumbnail> thumbnails;

        @SerializedName("media$content")
        private List<Content> contents;

        @SerializedName("media$ratings")
        private List<Rating> ratings;

        public String toString() {
            return id + " : " + title + " : " + series;
        }

        public String getThumbnail() {
            Thumbnail largest = null;
            if (thumbnails != null) {
                for (Thumbnail t : thumbnails) {
                    if (largest == null || t.width > largest.width) {
                        largest = t;
                    }
                }
            }
            if (largest != null) {
                return largest.url;
            }
            return thumbnail;
        }

        public List<String> getCategories() {
            List<String> cats = new ArrayList<>();
            if (categories != null) {
                for (Category c : categories) {
                    if ("Channel".equals(c.scheme) || "Genre".equals(c.scheme)) {
                        String name = c.getName();
                        if (name != null) {
                            cats.add(c.scheme + "/" + name);
                        }
                    }
                }
            }
            return cats;
        }

        public String getChannel() {
            if (categories != null) {
                for (Category c : categories) {
                    if ("Channel".equals(c.scheme)) {
                        String name = c.getName();
                        if (name != null) {
                            return name;
                        }
                    }
                }
            }
            return "";
        }

        public String getRating() {
            if (ratings != null && ratings.size() > 0) {
                return ratings.get(0).rating;
            }
            return "";
        }

        public int getDuration() {
            if (contents != null && contents.size() > 0) {
                try {
                    Integer.parseInt(contents.get(0).duration);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            return 0;
        }
    }
}
