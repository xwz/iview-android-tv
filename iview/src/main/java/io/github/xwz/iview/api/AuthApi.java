package io.github.xwz.iview.api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.xwz.iview.BuildConfig;
import io.github.xwz.iview.content.ContentManager;

public class AuthApi extends IViewApi {

    private static final String TAG = "AuthApi";
    private static final Uri AUTH_URL = Uri.parse(BuildConfig.AUTH_URL);
    private final String id;

    private static final Pattern TOKEN_PATTERN = Pattern.compile("<tokenhd>([^<]+)</tokenhd>");
    private static final Pattern SERVER_PATTERN = Pattern.compile("<server>([^<]+)</server>");
    private static final Pattern HOST_PATTERN = Pattern.compile("^(https?://)([^/]+)(/?.*)$");

    public AuthApi(Context context, String id) {
        super(context);
        setEnableCache(false);
        this.id = id;
    }

    @Override
    protected Void doInBackground(String... urls) {
        if (urls.length > 0) {
            buildAuth(urls[0]);
        }
        return null;
    }

    private void buildAuth(String stream) {
        if (stream == null) {
            stream = updateEpisodeDetails(id);
        }
        if (stream != null) {
            Pair<String, String> auth = getAuthToken();
            if (auth != null) {
                Uri.Builder builder = Uri.parse(stream).buildUpon();
                builder.authority(auth.first).appendQueryParameter("hdnea", auth.second);
                Uri url = builder.build();
                Log.d(TAG, "Stream URL:" + url);
                ContentManager.cache().putStreamUrl(id, url);
                ContentManager.getInstance().broadcastChange(ContentManager.CONTENT_AUTH_DONE, id);
            } else {
                Log.w(TAG, "Unable to build stream URL for:" + id);
            }
        } else {
            Log.w(TAG, "Unable to find stream URL for:" + id);
        }
    }

    private String updateEpisodeDetails(String href) {
        Log.d(TAG, "Need to update episode details");
        EpisodeDetailsApi api = new EpisodeDetailsApi(getContext(), href);
        if (api.updateEpisode(href)) {
            EpisodeModel ep = (EpisodeModel) ContentManager.cache().getEpisode(href);
            return ep.getStream();
        } else {
            ContentManager.getInstance().broadcastChange(ContentManager.CONTENT_AUTH_ERROR, ContentManager.AUTH_FAILED_URL, id);
        }
        return null;
    }

    private Pair<String, String> getAuthToken() {
        String content = fetchUrlSkipLocalCache(AUTH_URL, 0);
        if (content != null) {
            String token = null;
            String host = null;
            Matcher t = TOKEN_PATTERN.matcher(content);
            if (t.find()) {
                token = t.group(1);
            }
            Matcher s = SERVER_PATTERN.matcher(content);
            if (s.find()) {
                Matcher h = HOST_PATTERN.matcher(s.group(1));
                if (h.find()) {
                    host = h.group(2);
                }
            }
            if (token != null && token.length() > 0 && host != null && host.length() > 0) {
                return new Pair<>(host, token);
            } else {
                ContentManager.getInstance().broadcastChange(ContentManager.CONTENT_AUTH_ERROR, ContentManager.AUTH_FAILED_TOKEN, id);
            }
        } else {
            ContentManager.getInstance().broadcastChange(ContentManager.CONTENT_AUTH_ERROR, ContentManager.AUTH_FAILED_NETWORK, id);
        }
        return null;
    }
}
