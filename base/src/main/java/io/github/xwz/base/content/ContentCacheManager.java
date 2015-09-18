package io.github.xwz.base.content;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.xwz.base.api.EpisodeBaseModel;
import io.github.xwz.base.trie.RadixTree;

public class ContentCacheManager {
    private static final String TAG = "ContentCacheManager";
    private final LocalBroadcastManager mBroadcastManager;

    private Map<String, EpisodeBaseModel> mEpisodes = new HashMap<>();
    private List<EpisodeBaseModel> mShows = new ArrayList<>();
    private RadixTree<String> mDictionary = new RadixTree<>();
    private final Map<String, Uri> mStreamUrls = new HashMap<>();
    private Map<String, List<EpisodeBaseModel>> mCollections = new LinkedHashMap<>();

    public ContentCacheManager(Context context) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public void broadcastChange(String change, String tag, String id) {
        Intent intent = new Intent(change);
        if (tag != null) {
            intent.putExtra(ContentManagerBase.CONTENT_TAG, tag);
        }
        if (id != null) {
            intent.putExtra(ContentManagerBase.CONTENT_ID, id);
        }
        Log.d(TAG, "Broadcast:=> " + change);
        if (mBroadcastManager != null) {
            mBroadcastManager.sendBroadcast(intent);
        }
    }

    public void broadcastChangeDelayed(long delay, final String change, final String tag, final String id) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                broadcastChange(change, tag, id);
            }
        }, delay);
    }

    public void broadcastChange(String change, String tag) {
        broadcastChange(change, tag, null);
    }

    public void broadcastChange(String change) {
        broadcastChange(change, null, null);
    }

    synchronized public List<EpisodeBaseModel> getAllShows() {
        return new ArrayList<>(mShows);
    }

    synchronized public boolean hasShows() {
        return !mShows.isEmpty();
    }

    synchronized public void putShows(Collection<EpisodeBaseModel> shows) {
        mShows = new ArrayList<>(shows);
    }

    public void putCollections(LinkedHashMap<String, List<EpisodeBaseModel>> collection) {
        mCollections = new LinkedHashMap<>(collection);
    }

    synchronized public LinkedHashMap<String, List<EpisodeBaseModel>> getCollections() {
        return new LinkedHashMap<>(mCollections);
    }

    synchronized public void putEpisodes(Collection<EpisodeBaseModel> episodes) {
        mEpisodes = new HashMap<>();
       addEpisodes(episodes);
    }

    synchronized public void addEpisodes(Collection<EpisodeBaseModel> episodes) {
        for (EpisodeBaseModel ep : episodes) {
            mEpisodes.put(ep.getHref(), ep);
        }
    }

    synchronized public void buildDictionary(Collection<EpisodeBaseModel> shows) {
        mDictionary = buildWordsFromShows(shows);
    }

    synchronized public List<String> getSuggestions(String query) {
        return mDictionary.getValuesWithPrefix(query);
    }

    synchronized public EpisodeBaseModel updateEpisode(EpisodeBaseModel ep) {
        if (mEpisodes.containsKey(ep.getHref())) {
            mEpisodes.get(ep.getHref());
        } else {
            mEpisodes.put(ep.getHref(), ep);
        }
        return mEpisodes.get(ep.getHref());
    }

    synchronized public EpisodeBaseModel getEpisode(String href) {
        if (href != null) {
            return mEpisodes.get(href);
        }
        return null;
    }

    synchronized public void putStreamUrl(String id, Uri url) {
        mStreamUrls.put(id, url);
    }

    synchronized public Uri getEpisodeStreamUrl(String id) {
        return mStreamUrls.get(id);
    }

    private RadixTree<String> buildWordsFromShows(Collection<EpisodeBaseModel> shows) {
        RadixTree<String> dict = new RadixTree<>();
        for (EpisodeBaseModel ep : shows) {
            dict.putAll(getWords(ep));
        }
        Log.d(TAG, "dict:" + dict.size());
        return dict;
    }

    private Map<String, String> getWords(EpisodeBaseModel episode) {
        Map<String, String> words = new HashMap<>();
        if (episode.getSeriesTitle() != null) {
            words.putAll(splitWords(episode.getSeriesTitle(), episode));
        }
        if (episode.getTitle() != null) {
            words.putAll(splitWords(episode.getTitle(), episode));
        }
        return words;
    }

    private Map<String, String> splitWords(String s, EpisodeBaseModel episode) {
        String[] words = s.split("\\s+");
        Map<String, String> result = new HashMap<>();
        for (String w : words) {
            String word = w.replaceAll("[^\\w]", "");
            if (word.length() >= 3) {
                result.put(word.toLowerCase(), word);
            }
        }
        return result;
    }
}