package io.github.xwz.base.content;

import android.app.SearchManager;
import android.content.Context;
import android.net.Uri;

import java.util.LinkedHashMap;
import java.util.List;

import io.github.xwz.base.models.IEpisodeModel;

public interface IContentManager {

    String CONTENT_ID = "io.github.xwz.base.CONTENT_ID";
    String CONTENT_TAG = "io.github.xwz.base.CONTENT_TAG";

    String CONTENT_SHOW_LIST_FETCHING = "io.github.xwz.base.CONTENT_SHOW_LIST_FETCHING";
    String CONTENT_SHOW_LIST_START = "io.github.xwz.base.CONTENT_SHOW_LIST_START";
    String CONTENT_SHOW_LIST_DONE = "io.github.xwz.base.CONTENT_SHOW_LIST_DONE";
    String CONTENT_SHOW_LIST_ERROR = "io.github.xwz.base.CONTENT_SHOW_LIST_ERROR";
    String CONTENT_SHOW_LIST_PROGRESS = "io.github.xwz.base.CONTENT_SHOW_LIST_PROGRESS";

    String CONTENT_EPISODE_FETCHING = "io.github.xwz.base.CONTENT_EPISODE_FETCHING";
    String CONTENT_EPISODE_START = "io.github.xwz.base.CONTENT_EPISODE_START";
    String CONTENT_EPISODE_DONE = "io.github.xwz.base.CONTENT_EPISODE_DONE";
    String CONTENT_EPISODE_ERROR = "io.github.xwz.base.CONTENT_EPISODE_ERROR";

    String CONTENT_AUTH_FETCHING = "io.github.xwz.base.CONTENT_AUTH_FETCHING";
    String CONTENT_AUTH_START = "io.github.xwz.base.CONTENT_AUTH_START";
    String CONTENT_AUTH_DONE = "io.github.xwz.base.CONTENT_AUTH_DONE";
    String CONTENT_AUTH_ERROR = "io.github.xwz.base.CONTENT_AUTH_ERROR";

    String AUTH_FAILED_NETWORK = "AUTH_FAILED_NETWORK";
    String AUTH_FAILED_TOKEN = "AUTH_FAILED_TOKEN";
    String AUTH_FAILED_URL = "AUTH_FAILED_URL";

    String OTHER_EPISODES = "OTHER_EPISODES";
    String MORE_LIKE_THIS = "More Like This";
    String GLOBAL_SEARCH_INTENT = "GLOBAL_SEARCH_INTENT";

    //The columns we'll include in the video database table
    String KEY_SERIES_TITLE = SearchManager.SUGGEST_COLUMN_TEXT_1;
    String KEY_TITLE = SearchManager.SUGGEST_COLUMN_TEXT_2;

    String KEY_IMAGE = SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE;
    String KEY_DATA_TYPE = SearchManager.SUGGEST_COLUMN_CONTENT_TYPE;
    String KEY_IS_LIVE = SearchManager.SUGGEST_COLUMN_IS_LIVE;
    String KEY_VIDEO_WIDTH = SearchManager.SUGGEST_COLUMN_VIDEO_WIDTH;
    String KEY_VIDEO_HEIGHT = SearchManager.SUGGEST_COLUMN_VIDEO_HEIGHT;
    String KEY_PRODUCTION_YEAR = SearchManager.SUGGEST_COLUMN_PRODUCTION_YEAR;
    String KEY_COLUMN_DURATION = SearchManager.SUGGEST_COLUMN_DURATION;
    String KEY_ACTION = SearchManager.SUGGEST_COLUMN_INTENT_ACTION;
    String KEY_EXTRA_DATA = SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA;
    String KEY_EXTRA_NAME = SearchManager.EXTRA_DATA_KEY;

    void updateRecommendations(Context context);
    LinkedHashMap<String, List<IEpisodeModel>> getAllShowsByCategories();
    void fetchShowList(boolean force);
    IEpisodeModel getEpisode(String href);
    void fetchEpisode(IEpisodeModel episode);
    List<String> suggestions(String query);
    List<IEpisodeModel> searchShows(String query);
    void fetchAuthToken(IEpisodeModel episode);
    Uri getEpisodeStreamUrl(IEpisodeModel episode);
    IEpisodeModel findNextEpisode(List<String> urls, String current);
}
