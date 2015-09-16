package io.github.xwz.base.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.BrowseFrameLayout;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.github.xwz.base.R;
import io.github.xwz.base.adapters.BaseArrayAdapter;
import io.github.xwz.base.adapters.CategoryPresenter;
import io.github.xwz.base.adapters.EpisodePresenter;
import io.github.xwz.base.content.IContentManager;
import io.github.xwz.base.api.CategoryModel;
import io.github.xwz.base.api.IEpisodeModel;

public abstract class MainFragment extends BrowseFragment {

    private static final String TAG = "MainFragment";
    private ProgressBar progress;
    private TextView progressText;
    private static final int SHOW_CATEGORY_COUNT = -1;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Action: " + action);
            if (IContentManager.CONTENT_SHOW_LIST_DONE.equals(action)) {
                updateAdapter();
                getContentManger().updateRecommendations(getActivity());
            } else if (IContentManager.CONTENT_SHOW_LIST_PROGRESS.equals(action)) {
                String msg = intent.getStringExtra(IContentManager.CONTENT_TAG);
                updateProgress(msg);
            }
        }
    };

    protected abstract IContentManager getContentManger();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);
        setupUIElements();
        setupListeners();
    }

    @SuppressWarnings("deprecation")
    protected void setupUIElements() {
        setHeadersState(HEADERS_DISABLED | HEADERS_HIDDEN);
        setupHeader();
        View root = getView();
        if (root != null) {
            progress = new ProgressBar(getActivity());
            progress.setLayoutParams(new FrameLayout.LayoutParams(150, 150, Gravity.CENTER));
            progressText = new TextView(getActivity());
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            lp.topMargin = 150;
            progressText.setLayoutParams(lp);
            progressText.setGravity(Gravity.CENTER);
            progressText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.fontHuge));
            BrowseFrameLayout frame = getBrowseFrame(root);
            frame.addView(progress);
            frame.addView(progressText);
        }
    }

    protected abstract void setupHeader();

    protected abstract BrowseFrameLayout getBrowseFrame(View root);

    private void hideProgress() {
        if (progress != null) {
            progress.setVisibility(View.GONE);
        }
        if (progressText != null) {
            progressText.setVisibility(View.GONE);
        }
    }

    private void updateProgress(String msg) {
        if (progressText != null) {
            progressText.setText(msg);
        }
    }

    private void setupListeners() {
        setOnSearchClickedListener(getSearchClickedListener());
        setOnItemViewClickedListener(getItemClickedListener());
    }

    private View.OnClickListener getSearchClickedListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), getSearchActivityClass());
                startActivity(intent);
            }
        };
    }

    private OnItemViewClickedListener getItemClickedListener() {
        return new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof CategoryModel) {
                    Intent intent = new Intent(getActivity(), getCategoryActivityClass());
                    intent.putExtra(IContentManager.CONTENT_ID, (IEpisodeModel) item);
                    startActivity(intent);
                } else if (item instanceof IEpisodeModel) {
                    Intent intent = new Intent(getActivity(), getDetailsActivityClass());
                    intent.putExtra(IContentManager.CONTENT_ID, (IEpisodeModel) item);
                    startActivity(intent);
                }
            }
        };
    }

    protected abstract Class<?> getSearchActivityClass();

    protected abstract Class<?> getDetailsActivityClass();

    protected abstract Class<?> getCategoryActivityClass();

    private void updateRows(ArrayObjectAdapter adapter) {
        LinkedHashMap<String, List<IEpisodeModel>> all = getContentManger().getAllShowsByCategories();
        int currentRows = adapter.size();
        int newRows = all.size();
        final EpisodePresenter card = new EpisodePresenter();
        final CategoryPresenter cat = new CategoryPresenter();
        PresenterSelector selector = new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object item) {
                return item instanceof CategoryModel ? cat : card;
            }
        };
        List<String> categories = new ArrayList<>(all.keySet());
        for (int i = 0; i < newRows; i++) {
            String category = categories.get(i);
            List<IEpisodeModel> episodes = all.get(category);
            if (SHOW_CATEGORY_COUNT > 0 && episodes.size() > SHOW_CATEGORY_COUNT) {
                CategoryModel collection = new CategoryModel(category);
                collection.setEpisodeCount(episodes.size());
                episodes.add(0, collection);
            }
            if (i < currentRows) { // update row
                ListRow row = (ListRow) adapter.get(i);
                row.setHeaderItem(new HeaderItem(category));
                BaseArrayAdapter<IEpisodeModel> items = (BaseArrayAdapter<IEpisodeModel>) row.getAdapter();
                items.replaceItems(episodes);
            } else { // add
                BaseArrayAdapter<IEpisodeModel> items = new BaseArrayAdapter<>(selector);
                items.addAll(0, episodes);
                HeaderItem header = new HeaderItem(category);
                ListRow row = new ListRow(header, items);
                adapter.add(row);
            }
        }
        int deleteRows = currentRows - newRows;
        if (deleteRows > 0) {
            adapter.removeItems(newRows, deleteRows);
        }
    }

    private void updateAdapter() {
        ArrayObjectAdapter adapter = (ArrayObjectAdapter) getAdapter();
        if (adapter == null) {
            adapter = new ArrayObjectAdapter(new ListRowPresenter());
            updateRows(adapter);
            setAdapter(adapter);
        } else {
            updateRows(adapter);
        }
        hideProgress();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
        boolean update = getAdapter() == null || getAdapter().size() == 0;
        getContentManger().fetchShowList(update);
    }

    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    private void registerReceiver() {
        Log.i(TAG, "Register receiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(IContentManager.CONTENT_SHOW_LIST_START);
        filter.addAction(IContentManager.CONTENT_SHOW_LIST_DONE);
        filter.addAction(IContentManager.CONTENT_SHOW_LIST_ERROR);
        filter.addAction(IContentManager.CONTENT_SHOW_LIST_PROGRESS);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }
}
