package io.github.xwz.base.adapters;

import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A simple array adapter to add and replace a list of items.
 */
public class BaseArrayAdapter<T> extends ObjectAdapter {

    private ArrayList<T> mItems = new ArrayList<>();

    public BaseArrayAdapter(Presenter presenter) {
        super(presenter);
    }

    public BaseArrayAdapter(PresenterSelector selector) {
        super(selector);
    }

    @Override
    public int size() {
        return mItems.size();
    }

    @Override
    public T get(int index) {
        return mItems.get(index);
    }

    public void addAll(int index, Collection<T> items) {
        int itemsCount = items.size();
        if (itemsCount == 0) {
            return;
        }
        mItems.addAll(index, items);
        notifyItemRangeInserted(index, itemsCount);
    }

    public void replaceItems(Collection<T> items) {
        int current = mItems.size();
        int newItems = items.size();
        mItems.clear();
        mItems.addAll(0, items);
        notifyItemRangeChanged(0, newItems);
        if (newItems > current) {
            notifyItemRangeInserted(current, newItems - current);
        } else if (newItems < current) {
            notifyItemRangeRemoved(newItems, current - newItems);
        }
    }
}