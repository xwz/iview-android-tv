package io.github.xwz.base.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.util.Log;
import android.view.View;

import java.util.List;

import io.github.xwz.base.Utils;
import io.github.xwz.base.adapters.EpisodePresenter;
import io.github.xwz.base.adapters.FilmPresenter;
import io.github.xwz.base.api.EpisodeBaseModel;
import io.github.xwz.base.content.ContentManagerBase;

public abstract class CategoryFragment extends VerticalGridFragment {
    private static final String TAG = "CategoryFragment";

    private static final int FILM_COLUMNS = 10;
    private static final int SHOW_COLUMNS = 5;
    private String category;
    private boolean isFilm = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        category = getActivity().getIntent().getStringExtra(ContentManagerBase.CONTENT_ID);
        isFilm = category.contains("Film/") || category.equals("Film");
        setupFragment();
        setupListeners();
        setupHeader();
    }

    protected void setupHeader() {
    }

    private void setupFragment() {
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(isFilm ? FILM_COLUMNS : SHOW_COLUMNS);
        setGridPresenter(gridPresenter);
        setTitle(Utils.stripCategory(category));

        List<EpisodeBaseModel> all = getContentManger().getAllShowsByCategory(category);
        ArrayObjectAdapter adapter;
        if (isFilm) {
           adapter = new ArrayObjectAdapter(new FilmPresenter(true));
        } else {
            adapter = new ArrayObjectAdapter(new EpisodePresenter());
        }
        adapter.addAll(0, all);

        setAdapter(adapter);
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
                if (item instanceof EpisodeBaseModel) {
                    Intent intent = new Intent(getActivity(), getDetailsActivityClass());
                    intent.putExtra(ContentManagerBase.CONTENT_ID, (EpisodeBaseModel) item);
                    startActivity(intent);
                }
            }
        };
    }

    protected abstract ContentManagerBase getContentManger();

    protected abstract Class<?> getSearchActivityClass();

    protected abstract Class<?> getDetailsActivityClass();
}
