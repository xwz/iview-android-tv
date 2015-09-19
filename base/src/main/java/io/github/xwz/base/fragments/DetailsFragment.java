package io.github.xwz.base.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.xwz.base.R;
import io.github.xwz.base.Utils;
import io.github.xwz.base.adapters.CardSelector;
import io.github.xwz.base.api.EpisodeBaseModel;
import io.github.xwz.base.api.PlayHistory;
import io.github.xwz.base.content.ContentManagerBase;
import io.github.xwz.base.views.EpisodeDetailsView;

public abstract class DetailsFragment extends android.support.v17.leanback.app.RowsFragment {

    private static final String TAG = "DetailsFragment";
    private EpisodeDetailsView mDetailView;
    private int mHeaderHeight;
    private EpisodeBaseModel mCurrentEpisode;
    private EpisodeBaseModel mLoadedEpisode;
    private ArrayObjectAdapter otherEpisodes;
    private boolean loadedOtherEpisodes = false;
    private List<String> mOtherEpisodeUrls = new ArrayList<>();

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Action: " + action + ", tag: " + intent.getStringExtra(ContentManagerBase.CONTENT_TAG));
            if (ContentManagerBase.CONTENT_EPISODE_DONE.equals(action)) {
                updateEpisodeData(intent);
            }
            if (ContentManagerBase.CONTENT_EPISODE_ERROR.equals(action)) {
                Utils.showToast(getActivity(), "Unable to find episode details.");
            }
        }
    };

    protected List<String> getOtherEpisodeUrls() {
        return mOtherEpisodeUrls;
    }

    protected abstract ContentManagerBase getContentManger();

    protected EpisodeBaseModel getCurrentEpisode() {
        return mCurrentEpisode;
    }

    protected EpisodeDetailsView getDetailView() {
        return mDetailView;
    }

    protected void setCurrentEpisode(EpisodeBaseModel ep) {
        mCurrentEpisode = ep;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EpisodeBaseModel episode = (EpisodeBaseModel) getActivity().getIntent().getSerializableExtra(ContentManagerBase.CONTENT_ID);
        if (episode == null) {
            episode = getEpisodeFromGlobalSearchIntent();
        }
        if (episode != null) {
            setupEpisode(episode);
        } else {
            Log.e(TAG, "No episode set.");
        }
    }

    private void setupEpisode(EpisodeBaseModel episode) {
        mLoadedEpisode = episode;
        selectCurrentEpisode(episode, true);

        Point size = Utils.getDisplaySize(getActivity());
        mHeaderHeight = Math.round(size.y * 0.475f);

        setupAdapter(episode);
        setupListeners();
    }

    private EpisodeBaseModel getEpisodeFromGlobalSearchIntent() {
        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        if (ContentManagerBase.GLOBAL_SEARCH_INTENT.equals(action)) {
            Log.d(TAG, "getEpisodeFromGlobalSearchIntent");
            Bundle data = intent.getExtras();
            if (data != null) {
                String href = data.getString(ContentManagerBase.KEY_EXTRA_NAME);
                Log.d(TAG, "Search result: " + href);
                return getContentManger().getEpisode(href);
            }
            Log.w(TAG, "Unable to find href from search result");
        }
        return null;
    }

    private void setupListeners() {
        setOnItemViewClickedListener(getItemClickedListener());
        setOnItemViewSelectedListener(getItemSelectedListener());
    }

    private void setupAdapter(EpisodeBaseModel episode) {
        ArrayObjectAdapter adapter = new ArrayObjectAdapter(new ListRowPresenter());
        otherEpisodes = new ArrayObjectAdapter(new CardSelector());
        otherEpisodes.add(0, episode);
        adapter.add(new ListRow(new HeaderItem(0, null), otherEpisodes));
        setAdapter(adapter);
    }

    protected void selectCurrentEpisode(EpisodeBaseModel episode, boolean initial) {
        Log.d(TAG, "Showing details: " + episode);
        if (!episode.equals(mCurrentEpisode)) {
            mCurrentEpisode = episode;
            getContentManger().fetchEpisode(episode);
            if (mDetailView != null) {
                mDetailView.setEpisode(episode);
            }
        }
    }

    private void updateEpisodeData(Intent intent) {
        String href = intent.getStringExtra(ContentManagerBase.CONTENT_TAG);
        EpisodeBaseModel ep = getContentManger().getEpisode(href);
        if (ep != null) {
            if (ep.equals(mCurrentEpisode)) {
                mCurrentEpisode.merge(ep);
                mDetailView.updateEpisode(mCurrentEpisode);
            }
            if (ep.equals(mLoadedEpisode) && !loadedOtherEpisodes) {
                updateRelatedEpisodes(ep.getOtherEpisodes());
                loadedOtherEpisodes = true;
                mOtherEpisodeUrls = mLoadedEpisode.getOtherEpisodeUrls(ContentManagerBase.OTHER_EPISODES);
                Log.d(TAG, "Other episodes:" + mOtherEpisodeUrls);
            }
        }
    }

    private void updateRelatedEpisodes(Map<String, List<EpisodeBaseModel>> others) {
        boolean updated = false;
        ArrayObjectAdapter adapter = (ArrayObjectAdapter) getAdapter();
        for (Map.Entry<String, List<EpisodeBaseModel>> list : others.entrySet()) {
            String title = list.getKey();
            Log.d(TAG, "More: " + title);
            if (ContentManagerBase.OTHER_EPISODES.equals(title)) {
                otherEpisodes.addAll(otherEpisodes.size(), list.getValue());
            } else {
                ArrayObjectAdapter more = new ArrayObjectAdapter(new CardSelector());
                more.addAll(0, list.getValue());
                adapter.add(new ListRow(new HeaderItem(0, title), more));
            }
            updated = true;
        }
        if (updated) {
            adapter.notifyArrayItemRangeChanged(0, adapter.size());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view instanceof FrameLayout) {
            insertHeader(inflater, (FrameLayout) view);
        }
        return view;
    }

    private void insertHeader(LayoutInflater inflater, FrameLayout container) {
        VerticalGridView grid = findFirstGrid(container);
        if (grid != null) {
            View header = inflater.inflate(R.layout.episode_details_view, container, false);
            View bottom = inflater.inflate(R.layout.dark_gradient, container, false);
            container.addView(header, 0);
            container.addView(bottom, 1);
            setupGridAlignment(grid);
            setupHeaderView(grid, header);
            setupGradient(bottom);
        } else {
            Utils.showToast(getActivity(), "No Grid Found!");
        }
    }

    private void setupGradient(View bottom) {
        int height = getResources().getDimensionPixelSize(R.dimen.episode_detail_padding_bottom);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) bottom.getLayoutParams();
        lp.topMargin = mHeaderHeight;
        lp.height = height;
        bottom.setLayoutParams(lp);
    }

    private void setupHeaderView(VerticalGridView grid, View header) {
        mDetailView = new EpisodeDetailsView(getActivity(), header);
        if (mCurrentEpisode != null) {
            mDetailView.setEpisode(mCurrentEpisode);
        }
        int height = getResources().getDimensionPixelSize(R.dimen.episode_detail_padding_bottom);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) header.getLayoutParams();
        lp.height = mHeaderHeight + height;
        header.setLayoutParams(lp);
    }

    private void setupGridAlignment(VerticalGridView grid) {
        int cardHeight = getResources().getDimensionPixelSize(R.dimen.card_height);
        int titleHeight = getResources().getDimensionPixelSize(R.dimen.lb_browse_header_height);
        grid.setWindowAlignment(VerticalGridView.WINDOW_ALIGN_LOW_EDGE);
        grid.setWindowAlignmentOffset(cardHeight + titleHeight);
        grid.setWindowAlignmentOffsetPercent(VerticalGridView.WINDOW_ALIGN_OFFSET_PERCENT_DISABLED);

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) grid.getLayoutParams();
        lp.topMargin = mHeaderHeight;
        grid.setLayoutParams(lp);
    }

    private VerticalGridView findFirstGrid(ViewGroup container) {
        for (int i = 0, k = container.getChildCount(); i < k; i++) {
            View view = container.getChildAt(i);
            if (view instanceof VerticalGridView) {
                return (VerticalGridView) view;
            }
        }
        return null;
    }

    protected abstract Class<?> getPlayerActivityClass();

    private OnItemViewClickedListener getItemClickedListener() {
        return new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                Log.d(TAG, "Clicked item:" + item);
                if (item instanceof EpisodeBaseModel) {
                    EpisodeBaseModel ep = (EpisodeBaseModel) item;
                    Intent intent = new Intent(getActivity(), getPlayerActivityClass());
                    intent.putExtra(ContentManagerBase.CONTENT_ID, ep);
                    String[] others = getOtherEpisodeUrls().toArray(new String[getOtherEpisodeUrls().size()]);
                    intent.putExtra(ContentManagerBase.OTHER_EPISODES, others);
                    startActivity(intent);
                }
            }
        };
    }

    private OnItemViewSelectedListener getItemSelectedListener() {
        return new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                Log.d(TAG, "Selected item:" + item);
                if (item instanceof EpisodeBaseModel) {
                    selectCurrentEpisode((EpisodeBaseModel) item, false);
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
    }

    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    private void registerReceiver() {
        Log.i(TAG, "Register receiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ContentManagerBase.CONTENT_EPISODE_START);
        filter.addAction(ContentManagerBase.CONTENT_EPISODE_DONE);
        filter.addAction(ContentManagerBase.CONTENT_EPISODE_ERROR);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }
}
