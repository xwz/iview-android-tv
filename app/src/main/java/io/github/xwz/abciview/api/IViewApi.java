package io.github.xwz.abciview.api;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StatFs;
import android.util.Log;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.github.xwz.abciview.BuildConfig;

abstract class IViewApi extends AsyncTask<String, Void, Void> {

    private static final String TAG = "IViewApi";
    private static final String API_URL = BuildConfig.API_URL;

    private static final String CACHE_PATH = "iview-api";
    private static final int MIN_DISK_CACHE_SIZE = 8 * 1024 * 1024;       // 8MB
    private static final int MAX_DISK_CACHE_SIZE = 64 * 1024 * 1024;      // 64MB

    private static final float MAX_AVAILABLE_SPACE_USE_FRACTION = 0.5f;
    private static final float MAX_TOTAL_SPACE_USE_FRACTION = 0.1f;

    private final OkHttpClient client = new OkHttpClient();

    private Context mContext = null;
    private boolean useCache = true;

    IViewApi(Context context) {
        mContext = context;
    }

    private CacheControl allowStaleCache(int seconds) {
        return new CacheControl.Builder().maxStale(seconds, TimeUnit.SECONDS).build();
    }

    void setEnableCache(boolean enable) {
        useCache = enable;
    }

    private Cache createCache(Context context) {
        File cacheDir = createDefaultCacheDir(context, CACHE_PATH);
        long cacheSize = calculateDiskCacheSize(cacheDir);
        Log.i(TAG, "iview API disk cache:" + cacheDir + ", size:" + (cacheSize / 1024 / 1024) + "MB");
        return new Cache(cacheDir, cacheSize);
    }

    Uri buildApiUrl(String path) {
        return buildApiUrl(path, null);
    }

    Uri buildApiUrl(String path, Map<String, String> params) {
        Uri.Builder uri = Uri.parse(API_URL).buildUpon();
        uri.appendPath(path);
        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                uri.appendQueryParameter(param.getKey(), param.getValue());
            }
        }
        return uri.build();
    }

    Context getContext() {
        return mContext;
    }

    JSONObject parseJSON(String content) {
        if (content != null) {
            try {
                return new JSONObject(content);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    String fetchUrl(Uri url, int staleness) {
        if (useCache && client.getCache() == null) {
            Cache cache = createCache(getContext());
            if (cache != null) {
                client.setCache(cache);
            }
        }
        return fetchFromNetwork(url, staleness);
    }

    String fetchFromNetwork(Uri url, int staleness) {
        Request.Builder builder = new Request.Builder();
        builder.url(url.toString());
        if (staleness > 0) {
            builder.cacheControl(allowStaleCache(staleness));
        }
        Request request = builder.build();
        Log.d(TAG, "Requesting URL:" + request.urlString());
        try {
            Response response = client.newCall(request).execute();
            if (response.cacheResponse() != null) {
                Log.d(TAG, "Cached response [" + response.code() + "]:" + request.urlString());
            } else {
                Log.d(TAG, "Network response [" + response.code() + "]:" + request.urlString());
            }
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @SuppressWarnings({"unchecked", "TypeParameterExplicitlyExtendsObject"})
    static <T extends Object> T get(JSONObject data, String key, T fallback) {
        T value = null;
        try {
            value = data != null && key != null ? (T) data.get(key) : null;
        } catch (JSONException e) {
            //Log.d(TAG, "No string value for: " + key);
        }
        return value != null ? value : fallback;
    }

    private static File createDefaultCacheDir(Context context, String path) {
        File cacheDir = context.getApplicationContext().getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getApplicationContext().getCacheDir();
        }
        File cache = new File(cacheDir, path);
        if (!cache.exists()) {
            //noinspection ResultOfMethodCallIgnored
            cache.mkdirs();
        }
        return cache;
    }

    private static long calculateDiskCacheSize(File dir) {
        long size = Math.min(calculateAvailableCacheSize(dir), MAX_DISK_CACHE_SIZE);
        return Math.max(size, MIN_DISK_CACHE_SIZE);
    }

    @SuppressWarnings("deprecation")
    private static long calculateAvailableCacheSize(File dir) {
        long size = 0;
        try {
            StatFs statFs = new StatFs(dir.getAbsolutePath());
            int sdkInt = Build.VERSION.SDK_INT;
            long totalBytes;
            long availableBytes;
            if (sdkInt < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                int blockSize = statFs.getBlockSize();
                availableBytes = ((long) statFs.getAvailableBlocks()) * blockSize;
                totalBytes = ((long) statFs.getBlockCount()) * blockSize;
            } else {
                availableBytes = statFs.getAvailableBytes();
                totalBytes = statFs.getTotalBytes();
            }
            size = (long) Math.min(availableBytes * MAX_AVAILABLE_SPACE_USE_FRACTION, totalBytes * MAX_TOTAL_SPACE_USE_FRACTION);
        } catch (IllegalArgumentException ignored) {
            // ignored
        }
        return size;
    }
}
