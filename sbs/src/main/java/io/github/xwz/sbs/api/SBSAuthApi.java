package io.github.xwz.sbs.api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.xwz.sbs.content.ContentManager;

public class SBSAuthApi extends SBSApiBase {
    private static final String TAG = "SBSAuthApi";
    private final String id;
    private static final Pattern SRC_PATTERN = Pattern.compile("<video src=\"([^\"]+)\"");

    public SBSAuthApi(Context context, String id) {
        super(context);
        setEnableCache(false);
        this.id = id;
    }

    @Override
    protected Void doInBackground(String... urls) {
        Log.d(TAG, "Doing AUTH");
        if (urls.length > 0) {
            buildAuth(urls[0]);
        }
        return null;
    }

    private void buildAuth(String href) {
        Uri url = createStreamUrl(href);
        if (url != null) {
            String content = fetchUrlSkipLocalCache(url, 0);
            if (content != null) {
                parseContent(content);
            } else {
                ContentManager.getInstance().broadcastChange(ContentManager.CONTENT_AUTH_ERROR, ContentManager.AUTH_FAILED_NETWORK, id);
            }
        }
    }

    private void parseContent(String content) {
        String f = "var playerParams = {";
        int pos = content.indexOf(f);
        if (pos >= 0) {
            int start = pos + f.length();
            int end = content.indexOf("};", start);
            if (end > pos) {
                String data = content.substring(start - 1, end + 1);
                Log.d(TAG, "data:" + data);
                Gson gson = new Gson();
                try {
                    PlayerParams params = gson.fromJson(data, PlayerParams.class);
                    Log.d(TAG, "params: " + params);
                    if (params != null && params.releaseUrls != null) {
                        String release = params.releaseUrls.getUrl();
                        if (release != null && release.length() > 0) {
                            Uri.Builder builder = Uri.parse(release).buildUpon();
                            Uri url = builder.build();
                            loadPlayList(url);
                            return;
                        }
                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        ContentManager.getInstance().broadcastChange(ContentManager.CONTENT_AUTH_ERROR, ContentManager.AUTH_FAILED_TOKEN, id);
    }

    private void loadPlayList(Uri url) {
        String content = fetchUrlSkipLocalCache(url, 0);
        if (content != null) {
            parsePlayList(content);
        } else {
            ContentManager.getInstance().broadcastChange(ContentManager.CONTENT_AUTH_ERROR, ContentManager.AUTH_FAILED_NETWORK, id);
        }
    }

    protected void onPreExecute() {
        ContentManager.getInstance().broadcastChange(ContentManager.CONTENT_AUTH_START);
    }

    private void parsePlayList(String content) {
        Matcher m = SRC_PATTERN.matcher(content);
        if (m.find()) {
            Uri.Builder builder = Uri.parse(m.group(1)).buildUpon();
            Uri url = builder.build();
            Log.d(TAG, "Stream URL:" + url);
            ContentManager.cache().putStreamUrl(id, url);
            ContentManager.getInstance().broadcastChange(ContentManager.CONTENT_AUTH_DONE, id);
        } else {
            ContentManager.getInstance().broadcastChange(ContentManager.CONTENT_AUTH_ERROR, ContentManager.AUTH_FAILED_TOKEN, id);
        }
    }

    static class ReleaseUrl {
        private String progressive;
        private String html;
        private String standard;

        public String getUrl() {
            if (html != null) {
                String url = html.toLowerCase();
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    return html;
                }
                if (url.startsWith("//")) {
                    return "http:" + html;
                }
                Log.d(TAG, "Invalid release URL: " + this);
            }
            return null;
        }

        public String toString() {
            return progressive + " | " + html + " | " + standard;
        }

    }

    static class PlayerParams {
        private ReleaseUrl releaseUrls;

        public String toString() {
            return "url:" + releaseUrls;
        }
    }
}
