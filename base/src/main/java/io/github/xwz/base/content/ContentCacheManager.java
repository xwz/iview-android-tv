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

import io.github.xwz.base.models.IEpisodeModel;
import io.github.xwz.base.trie.RadixTree;

public class ContentCacheManager {
    private static final String TAG = "ContentCacheManager";
    private final LocalBroadcastManager mBroadcastManager;

    private final Map<String, IEpisodeModel> mEpisodes = new HashMap<>();
    private List<IEpisodeModel> mShows = new ArrayList<>();
    private RadixTree<String> mDictionary = new RadixTree<>();
    private final Map<String, Uri> mStreamUrls = new HashMap<>();
    private Map<String, List<IEpisodeModel>> mCollections = new LinkedHashMap<>();

    public ContentCacheManager(Context context) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public void broadcastChange(String change, String tag, String id) {
        Intent intent = new Intent(change);
        if (tag != null) {
            intent.putExtra(IContentManager.CONTENT_TAG, tag);
        }
        if (id != null) {
            intent.putExtra(IContentManager.CONTENT_ID, id);
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

    synchronized public List<IEpisodeModel> getAllShows() {
        return new ArrayList<>(mShows);
    }

    synchronized public boolean hasShows() {
        return !mShows.isEmpty();
    }

    synchronized public void putShows(Collection<IEpisodeModel> shows) {
        mShows = new ArrayList<>(shows);
    }

    synchronized public void addCollection(String title, List<IEpisodeModel> shows) {
        mCollections.put(title, new ArrayList<>(shows));
    }

    synchronized public LinkedHashMap<String, List<IEpisodeModel>> getCollections() {
        return new LinkedHashMap<>(mCollections);
    }

    synchronized public void addEpisodes(Collection<IEpisodeModel> episodes) {
        for (IEpisodeModel ep : episodes) {
            mEpisodes.put(ep.getHref(), ep);
        }
    }

    synchronized public void setDictionary(RadixTree<String> dict) {
        mDictionary = dict;
    }

    synchronized public List<String> getSuggestions(String query) {
        return mDictionary.getValuesWithPrefix(query);
    }

    synchronized public void updateEpisode(IEpisodeModel ep) {
        if (mEpisodes.containsKey(ep.getHref())) {
            mEpisodes.get(ep.getHref()).merge(ep);
        } else {
            mEpisodes.put(ep.getHref(), ep);
        }
    }

    synchronized public IEpisodeModel getEpisode(String href) {
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
}