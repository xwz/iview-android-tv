package io.github.xwz.base.content;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public abstract class SearchProviderBase extends ContentProvider {

    private static String TAG = "SearchProviderBase";
    private static final int SEARCH_SUGGEST = 0;
    private static final int REFRESH_SHORTCUT = 1;

    protected abstract String getAuthority();
    protected abstract Cursor getSuggestions(String query);

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate");
        return false;
    }

    private UriMatcher getUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get suggestions...
        Log.d(TAG, "suggest_uri_path_query: " + SearchManager.SUGGEST_URI_PATH_QUERY);
        matcher.addURI(getAuthority(), SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(getAuthority(), SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
        return matcher;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (getUriMatcher().match(uri)) {
            case SEARCH_SUGGEST:
                Log.d(TAG, "search suggest: " + selectionArgs[0] + " URI: " + uri);
                if (selectionArgs == null) {
                    throw new IllegalArgumentException(
                            "selectionArgs must be provided for the Uri: " + uri);
                }
                return getSuggestions(selectionArgs[0]);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri+" authority: " + getAuthority());
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (getUriMatcher().match(uri)) {
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            case REFRESH_SHORTCUT:
                return SearchManager.SHORTCUT_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
