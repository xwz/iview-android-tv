package io.github.xwz.abciview.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import io.github.xwz.abciview.R;
import io.github.xwz.abciview.activities.DetailsActivity;
import io.github.xwz.abciview.adapters.EpisodePresenter;
import io.github.xwz.abciview.content.ContentManager;
import io.github.xwz.abciview.models.EpisodeModel;

/**
 * Created by wei on 28/08/15.
 */
public class SearchFragment extends android.support.v17.leanback.app.SearchFragment
        implements android.support.v17.leanback.app.SearchFragment.SearchResultProvider {

    private static final String TAG = "SearchFragment";
    private static final int SEARCH_DELAY_MS = 150;

    private ArrayObjectAdapter adapter;
    private SearchRunnable searcher;
    private Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSearchResultProvider(this);
        setSpeechRecognitionCallback(null);
        adapter = new ArrayObjectAdapter(new ListRowPresenter());
        searcher = new SearchRunnable();
        setOnItemViewClickedListener(getItemClickedListener());
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        return adapter;
    }

    private OnItemViewClickedListener getItemClickedListener() {
        return new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof EpisodeModel) {
                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
                    intent.putExtra(ContentManager.CONTENT_ID, (EpisodeModel) item);
                    startActivity(intent);
                }
            }
        };
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (!TextUtils.isEmpty(query)) {
            List<String> suggestions = ContentManager.getInstance().suggestions(query);
            Log.d(TAG, "Suggestions: " + suggestions);
            displayCompletions(suggestions);
        } else {
            displayCompletions(null);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.clear();
        if (!TextUtils.isEmpty(query)) {
            searcher.setQuery(query);
            handler.removeCallbacks(searcher);
            handler.postDelayed(searcher, SEARCH_DELAY_MS);
        }
        return true;
    }

    private class SearchRunnable implements Runnable {

        private String query;

        public void setQuery(String str) {
            query = str;
        }

        @Override
        public void run() {
            List<EpisodeModel> results = ContentManager.getInstance().searchShows(query);
            EpisodePresenter card = new EpisodePresenter();
            ArrayObjectAdapter row = new ArrayObjectAdapter(card);
            row.addAll(0, results);
            HeaderItem header = new HeaderItem(0, getResources().getString(R.string.search_results));
            adapter.add(new ListRow(header, row));
        }
    }
}
