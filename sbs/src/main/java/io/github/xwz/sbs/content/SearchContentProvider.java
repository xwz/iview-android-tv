package io.github.xwz.sbs.content;

import android.database.Cursor;

import io.github.xwz.base.content.SearchProviderBase;

public class SearchContentProvider extends SearchProviderBase {

    public static String AUTHORITY = "io.github.xwz.sbs.search";

    @Override
    protected String getAuthority() {
        return AUTHORITY;
    }

    protected Cursor getSuggestions(String query) {
        return ContentManager.getInstance().searchShowsCursor(query);
    }
}
