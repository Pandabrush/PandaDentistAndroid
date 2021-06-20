package com.pandadentist.widget;

import java.util.ArrayList;
import java.util.List;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.pandadentist.bleconnection.utils.Util;

/**
 * Author: 张维亚
 * 创建时间：2014年6月19日 下午3:11:17
 * 修改时间：2014年6月19日 下午3:11:17
 * Description: 适配器基类
 **/
public class ZBaseAdapter<T> extends BaseAdapter implements ZRefreshAdapterCallBack<T> {

    private final int ADD_END = -1;
    private OnItemLoadingView<T> mLoadingItemView = null;
    protected List<T> mItems = new ArrayList<T>();

    public ZBaseAdapter(List<T> items, OnItemLoadingView<T> loadingView) {
        this.setItems(items);
        this.mLoadingItemView = loadingView;
    }

    @Override
    public void add(T t) {
        this.add(t, ADD_END);
    }

    @Override
    public void add(T t, int position) {
        this.addItem(t, position);
        this.notifyDataSetChanged();
    }

    @Override
    public void addAll(List<T> list) {
        this.addAll(list, ADD_END);
    }

    @Override
    public void addAll(List<T> list, int position) {
        this.addItems(list, position);
        this.notifyDataSetChanged();
    }

    @Override
    public void addCurrentAll(List<Current<T>> list) {
        if (Util.isEmpty(list))
            return;

        for (Current<T> item : list)
            this.addItem(item.t, item.position);

        this.notifyDataSetChanged();
    }

    @Override
    public void remove(T t) {
        this.mItems.remove(t);
        this.notifyDataSetChanged();
    }

    @Override
    public void remove(int position) {
        this.removeItem(position);
        this.notifyDataSetChanged();
    }

    @Override
    public void removeAll(List<T> list) {
        if (Util.isEmpty(list))
            return;
        this.mItems.removeAll(list);
        this.notifyDataSetChanged();
    }

    @Override
    public void replace(T t, int position) {
        this.replaceItem(t, position);
        this.notifyDataSetChanged();
    }

    @Override
    public void replaceCurrentAll(List<Current<T>> list) {
        if (Util.isEmpty(list))
            return;

        for (Current<T> item : list)
            this.replaceItem(item.t, item.position);

        this.notifyDataSetChanged();
    }

    @Override
    public void reload(List<T> list) {
        this.setItems(list);
        this.notifyDataSetChanged();
    }

    @Override
    public void clear() {
        this.mItems.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.mItems.size();
    }

    @Override
    public T getItem(int position) {
        return this.mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        return this.mLoadingItemView.getView(convertView, getItem(position));
    }

    private void setItems(List<T> list) {
        if (this.mItems == list)
            return;
        this.mItems.clear();
        this.mItems.addAll(list);
    }

    public boolean addItem(T t, int position) {
        if (addlast(position))
            return mItems.add(t);

        mItems.add(position, t);
        return true;
    }

    private boolean addItems(List<T> items, int position) {
        if (addlast(position))
            return this.mItems.addAll(items);

        return this.mItems.addAll(position, items);
    }

    private boolean addlast(int position) {
        return position == ADD_END || position < 0 || position >= getCount();
    }

    private void replaceItem(T t, int position) {
        this.removeItem(position);
        this.addItem(t, position);
    }

    private T removeItem(int position) {
        return this.mItems.remove(position);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }

    public interface OnItemLoadingView<T> {
        View getView(View convertView, T item);
    }
}