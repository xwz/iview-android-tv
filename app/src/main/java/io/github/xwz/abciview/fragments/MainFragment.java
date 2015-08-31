package io.github.xwz.abciview.fragments;

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
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.util.List;
import java.util.Map;

import io.github.xwz.abciview.R;
import io.github.xwz.abciview.activities.DetailsActivity;
import io.github.xwz.abciview.activities.SearchActivity;
import io.github.xwz.abciview.adapters.EpisodePresenter;
import io.github.xwz.abciview.content.ContentManager;
import io.github.xwz.abciview.models.EpisodeModel;

public class MainFragment extends BrowseFragment {

    private static final String TAG = "MainFragment";
    private ProgressBar progress;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Action: " + action);
            if (ContentManager.CONTENT_SHOW_LIST_DONE.equals(action)) {
                updateAdapter();
            }
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);
        ContentManager.getInstance().fetchShowList();
        setupUIElements();
        setupListeners();
    }

    @SuppressWarnings("deprecation")
    private void setupUIElements() {
        setHeadersState(HEADERS_DISABLED | HEADERS_HIDDEN);
        setBadgeDrawable(getResources().getDrawable(R.mipmap.logo));
        setSearchAffordanceColor(getResources().getColor(R.color.green_500));

        View root = getView();
        if (root != null) {
            BrowseFrameLayout frame = (BrowseFrameLayout) root.findViewById(android.support.v17.leanback.R.id.browse_frame);
            progress = new ProgressBar(getActivity());
            progress.setLayoutParams(new FrameLayout.LayoutParams(150, 150, Gravity.CENTER));
            frame.addView(progress);
        }
    }

    private void hideProgress() {
        if (progress != null) {
            progress.setVisibility(View.GONE);
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
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        };
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

    private void updateAdapter() {
        Map<String, List<EpisodeModel>> all = ContentManager.getInstance().getAllShowsByCategories();
        ArrayObjectAdapter adapter = new ArrayObjectAdapter(new ListRowPresenter());
        EpisodePresenter card = new EpisodePresenter();
        int i = 0;
        for (Map.Entry<String, List<EpisodeModel>> channel : all.entrySet()) {
            ArrayObjectAdapter row = new ArrayObjectAdapter(card);
            row.addAll(0, channel.getValue());
            HeaderItem header = new HeaderItem(i++, channel.getKey());
            adapter.add(new ListRow(header, row));
        }
        setAdapter(adapter);
        hideProgress();
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
        filter.addAction(ContentManager.CONTENT_SHOW_LIST_START);
        filter.addAction(ContentManager.CONTENT_SHOW_LIST_DONE);
        filter.addAction(ContentManager.CONTENT_SHOW_LIST_ERROR);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }
}
