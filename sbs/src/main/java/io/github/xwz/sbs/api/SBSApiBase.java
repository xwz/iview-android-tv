package io.github.xwz.sbs.api;

import android.content.Context;
import android.net.Uri;

import java.util.Map;

import io.github.xwz.base.api.HttpApiBase;
import io.github.xwz.sbs.BuildConfig;

abstract class SBSApiBase extends HttpApiBase {

    private static final String API_URL = BuildConfig.API_URL;
    private static final String CACHE_PATH = "sbs-api";

    public SBSApiBase(Context context) {
        super(context);
    }

    protected String getCachePath() {
        return CACHE_PATH;
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
}
