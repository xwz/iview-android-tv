package io.github.xwz.base.api;

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
import com.squareup.okhttp.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.github.xwz.base.trie.RadixTree;

public abstract class HttpApiBase extends AsyncTask<String, Void, Void> {

    private static final String TAG = "HttpApiBase";

    private static final int MIN_DISK_CACHE_SIZE = 8 * 1024 * 1024;       // 8MB
    private static final int MAX_DISK_CACHE_SIZE = 64 * 1024 * 1024;      // 64MB

    private static final float MAX_AVAILABLE_SPACE_USE_FRACTION = 0.5f;
    private static final float MAX_TOTAL_SPACE_USE_FRACTION = 0.1f;

    private final OkHttpClient client = new OkHttpClient();

    private Context mContext = null;
    private boolean useCache = true;

    public HttpApiBase(Context context) {
        mContext = context;
    }

    protected CacheControl allowStaleCache(int seconds) {
        return new CacheControl.Builder().maxStale(seconds, TimeUnit.SECONDS).build();
    }

    protected void setEnableCache(boolean enable) {
        useCache = enable;
    }

    private Cache createCache(Context context) {
        File cacheDir = createDefaultCacheDir(context, getCachePath());
        long cacheSize = calculateDiskCacheSize(cacheDir);
        Log.i(TAG, "iview API disk cache:" + cacheDir + ", size:" + (cacheSize / 1024 / 1024) + "MB");
        return new Cache(cacheDir, cacheSize);
    }

    abstract protected String getCachePath();

    protected Context getContext() {
        return mContext;
    }

    protected JSONObject parseJSON(String content) {
        if (content != null) {
            try {
                return new JSONObject(content);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void ensureCache() {
        if (useCache && client.getCache() == null) {
            Cache cache = createCache(getContext());
            if (cache != null) {
                client.setCache(cache);
            }
        }
    }

    protected String fetchUrl(Uri url, int staleness) {
        ensureCache();
        return extractBody(fetchFromNetwork(url, staleness));
    }

    protected String fetchUrlSkipLocalCache(Uri url, int staleness) {
        return extractBody(fetchFromNetwork(url, staleness));
    }

    private String extractBody(ResponseBody body) {
        if (body != null) {
            try {
                return body.string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    protected InputStream fetchStream(Uri url, int staleness) {
        ensureCache();
        ResponseBody body = fetchFromNetwork(url, staleness);
        if (body != null) {
            try {
                return body.byteStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private ResponseBody fetchFromNetwork(Uri url, int staleness) {
        Request.Builder builder = new Request.Builder();
        builder.url(url.toString());
        if (staleness > 0) {
            builder.cacheControl(allowStaleCache(staleness));
        }
        Request request = builder.build();
        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(60, TimeUnit.SECONDS);
        Log.d(TAG, "Requesting URL:" + request.urlString());
        try {
            Response response = client.newCall(request).execute();
            if (response.cacheResponse() != null) {
                Log.d(TAG, "Cached response [" + response.code() + "]:" + request.urlString());
            } else {
                Log.d(TAG, "Network response [" + response.code() + "]:" + request.urlString());
            }
            if (response.isSuccessful()) {
                return response.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "TypeParameterExplicitlyExtendsObject"})
    protected static <T extends Object> T get(JSONObject data, String key, T fallback) {
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