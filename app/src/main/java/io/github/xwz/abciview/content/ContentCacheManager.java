package io.github.xwz.abciview.content;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.xwz.abciview.models.EpisodeModel;
import io.github.xwz.abciview.trie.RadixTree;

public class ContentCacheManager {
    private static final String TAG = "ContentCacheManager";
    private LocalBroadcastManager mBroadcastManager = null;

    private final Map<String, EpisodeModel> mEpisodes = new HashMap<>();
    private List<EpisodeModel> mShows = new ArrayList<>();
    private RadixTree<String> mDictionary = new RadixTree<>();
    private final Map<String, Uri> mStreamUrls = new HashMap<>();

    public ContentCacheManager(Context context) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public void broadcastChange(String change, String tag, String id) {
        Intent intent = new Intent(change);
        if (tag != null) {
            intent.putExtra(ContentManager.CONTENT_TAG, tag);
        }
        if (id != null) {
            intent.putExtra(ContentManager.CONTENT_ID, id);
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

    synchronized public List<EpisodeModel> getAllShows() {
        return new ArrayList<>(mShows);
    }

    synchronized public boolean hasShows() {
        return !mShows.isEmpty();
    }

    synchronized public void putShows(List<EpisodeModel> shows) {
        mShows = new ArrayList<>(shows);
    }

    synchronized public void addEpisodes(Collection<EpisodeModel> episodes) {
        for (EpisodeModel ep : episodes) {
            mEpisodes.put(ep.getHref(), ep);
        }
    }

    synchronized public void setDictionary(RadixTree<String> dict) {
        mDictionary = dict;
    }

    synchronized public List<String> getSuggestions(String query) {
        return mDictionary.getValuesWithPrefix(query);
    }

    synchronized public void updateEpisode(EpisodeModel ep) {
        if (mEpisodes.containsKey(ep.getHref())) {
            mEpisodes.get(ep.getHref()).merge(ep);
        } else {
            mEpisodes.put(ep.getHref(), ep);
        }
    }

    synchronized public EpisodeModel getEpisode(String href) {
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