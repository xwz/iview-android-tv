package io.github.xwz.base.api;

import android.util.Log;

import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.xwz.base.content.ContentCacheManager;
import io.github.xwz.base.content.ContentManagerBase;

public class ContentDatabaseCache {
    private static final String TAG = "ContentDatabaseCache";

    private static final String TYPE_EPISODES = "EPISODES";
    private static final String TYPE_COLLECTIONS = "COLLECTIONS";
    private static final String TYPE_SHOWS = "SHOWS";

    public void clearCache() {
        Log.d(TAG, "Clear db");
        FlowQueryList<EpisodeBaseModel> query = new FlowQueryList<>(EpisodeBaseModel.class);
        query.clear();
    }

    public void putShows(Collection<EpisodeBaseModel> shows) {
        Log.d(TAG, "store shows into db");
        FlowQueryList<EpisodeBaseModel> query = new FlowQueryList<>(EpisodeBaseModel.class);
        query.beginTransaction();
        for (EpisodeBaseModel ep : shows) {
            EpisodeBaseModel model = new EpisodeBaseModel();
            model.merge(ep);
            model.DATA_TYPE = TYPE_EPISODES;
            model.save();
        }
        query.endTransactionAndNotify();
    }

    public void putCollections(LinkedHashMap<String, List<EpisodeBaseModel>> collections) {
        Log.d(TAG, "store collections into db");
        int i = 0;
        FlowQueryList<EpisodeBaseModel> query = new FlowQueryList<>(EpisodeBaseModel.class);
        query.beginTransaction();
        for (Map.Entry<String, List<EpisodeBaseModel>> collection : collections.entrySet()) {
            Log.d(TAG, "Adding collection: " + collection.getKey() + " => " + collection.getValue().size());
            updateProgress("Loading " + collection.getKey() + "...");
            for (EpisodeBaseModel ep : collection.getValue()) {
                EpisodeBaseModel model = new EpisodeBaseModel();
                model.merge(ep);
                model.DATA_TYPE = TYPE_COLLECTIONS;
                model.DATA_COLLECTION_KEY = collection.getKey();
                model.DATA_COLLECTION_INDEX = i++;
                model.save();
            }
        }
        query.endTransactionAndNotify();
    }

    private void updateProgress(String str) {
        ContentManagerBase.getInstance().broadcastChange(ContentManagerBase.CONTENT_SHOW_LIST_PROGRESS, str);
    }

    public void putEpisodes(Collection<EpisodeBaseModel> episodes) {
        Log.d(TAG, "store episodes into db");
        FlowQueryList<EpisodeBaseModel> query = new FlowQueryList<>(EpisodeBaseModel.class);
        query.beginTransaction();
        for (EpisodeBaseModel ep : episodes) {
            EpisodeBaseModel model = new EpisodeBaseModel();
            model.merge(ep);
            model.DATA_TYPE = TYPE_SHOWS;
            model.save();
        }
        query.endTransactionAndNotify();
    }

    private List<EpisodeBaseModel> getModelsOfType(Class<?> model, String type, List<EpisodeBaseModel> existing, boolean uniqueSeries) {
        FlowCursorList<EpisodeBaseModel> cursor = new FlowCursorList<>(false, EpisodeBaseModel.class,
                Condition.column(EpisodeBaseModel$Table.DATA_TYPE).eq(type));
        Map<String, EpisodeBaseModel> all = new HashMap<>();
        for (int i = 0, k = cursor.getCount(); i < k; i++) {
            EpisodeBaseModel ep = (EpisodeBaseModel) createInstanceOf(model);
            if (ep != null) {
                int index = existing.indexOf(ep);
                if (index > -1) {
                    ep = existing.get(index);
                } else {
                    EpisodeBaseModel item = cursor.getItem(i);
                    item.unserialize();
                    ep.merge(item);
                }
                if (uniqueSeries) {
                    all.put(ep.getSeriesTitle(), ep);
                } else {
                    all.put(ep.getHref(), ep);
                }
            }
        }
        cursor.close();
        return new ArrayList<>(all.values());
    }

    private Object createInstanceOf(Class<?> type) {
        try {
            Constructor<?> ctor = type.getConstructor();
            Object object = ctor.newInstance();
            return object;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private LinkedHashMap<String, List<EpisodeBaseModel>> getCollections(Class<?> model, List<EpisodeBaseModel> existing) {
        LinkedHashMap<String, List<EpisodeBaseModel>> collections = new LinkedHashMap<>();
        FlowCursorList<EpisodeBaseModel> cursor = new FlowCursorList<>(false,
                (new Select()).from(EpisodeBaseModel.class)
                        .where(Condition.column(EpisodeBaseModel$Table.DATA_TYPE).eq(TYPE_COLLECTIONS))
                        .orderBy(true, EpisodeBaseModel$Table.DATA_COLLECTION_INDEX));
        for (int i = 0, k = cursor.getCount(); i < k; i++) {
            EpisodeBaseModel item = cursor.getItem(i);
            item.unserialize();
            int index = existing.indexOf(item);
            EpisodeBaseModel ep;
            if (index > -1) {
                ep = existing.get(index);
            } else {
                ep = (EpisodeBaseModel) createInstanceOf(model);
                if (ep != null) {
                    ep.merge(item);
                }
            }
            if (ep != null) {
                if (!collections.containsKey(item.DATA_COLLECTION_KEY)) {
                    collections.put(item.DATA_COLLECTION_KEY, new ArrayList<EpisodeBaseModel>());
                }
                collections.get(item.DATA_COLLECTION_KEY).add(ep);
            }
        }
        for (Map.Entry<String, List<EpisodeBaseModel>> collection : collections.entrySet()) {
            Log.d(TAG, "Loaded collection: " + collection.getKey() + " => " + collection.getValue().size());
        }
        return collections;
    }

    public boolean loadFromDbCache(ContentCacheManager cache, Class<?> type) {
        updateProgress("Loading images...");
        List<EpisodeBaseModel> episodes = getModelsOfType(type, TYPE_EPISODES, new ArrayList<EpisodeBaseModel>(), false);
        if (episodes.size() > 0) {
            updateProgress("Loading TV shows...");
            List<EpisodeBaseModel> shows = getModelsOfType(type, TYPE_SHOWS, episodes, true);
            updateProgress("Loading movies...");
            LinkedHashMap<String, List<EpisodeBaseModel>> collections = getCollections(type, episodes);
            cache.putEpisodes(episodes);
            cache.putShows(shows);
            cache.putCollections(collections);
            updateProgress("Loading content...");
            cache.buildDictionary(shows);
            Log.d(TAG, "Loaded data from database");
            return true;
        }
        return false;
    }
}
