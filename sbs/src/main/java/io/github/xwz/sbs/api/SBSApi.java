package io.github.xwz.sbs.api;

import android.content.Context;
import android.net.Uri;

import java.util.Map;

import io.github.xwz.base.ImmutableMap;

public class SBSApi extends SBSApiBase {

    private static final int CACHE_EXPIRY = 600; // 10 mins
    private static final int ITEMS_PER_PAGE = 10;

    public SBSApi(Context context) {
        super(context);
    }

    @Override
    protected Void doInBackground(String... params) {
        fetchAllTitles(0);

        return null;
    }

    private void fetchAllTitles(int page) {
        String response = fetchUrl(getIndexUrl(page), CACHE_EXPIRY);
        /*JSONObject data = parseJSON(response);
        List<IEpisodeModel> titles = getEpisodesFromData(data, true);
        for (IEpisodeModel ep : titles) {
            episodes.put(ep.getHref(), ep);
        }*/
    }

    private Uri getIndexUrl(int page) {
        String range = String.format("%d-%d", page * ITEMS_PER_PAGE, (page + 1) * ITEMS_PER_PAGE);
        Map<String, String> params = ImmutableMap.of("format", "json", "range", range);
        return buildApiUrl(params);
    }
}
