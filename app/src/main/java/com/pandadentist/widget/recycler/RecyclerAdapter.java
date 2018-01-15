package com.pandadentist.widget.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.pandadentist.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 张维亚(zhangwy) on 2016/12/13 下午2:11.
 * Updated by zhangwy on 2016/12/13 下午2:11.
 * Description RecyclerView 的适配器
 */
@SuppressWarnings("unused")
public class RecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> implements View.OnClickListener, RefreshAdapterCallBack<T> {
    private final int ADD_END = -1;
    private List<T> array = new ArrayList<>();
    private OnItemClickListener<T> itemClickListener;
    private OnItemLoading<T> itemLoading;

    public RecyclerAdapter(List<T> array, OnItemLoading<T> itemLoading) {
        super();
        this.setItems(array);
        this.itemLoading = itemLoading;
    }

    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        this.itemClickListener = listener;
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
    public void addCurrents(List<Current<T>> list) {
        if (Util.isEmpty(list))
            return;

        for (Current<T> item : list)
            this.addItem(item.t, item.position);

        this.notifyDataSetChanged();
    }

    @Override
    public void remove(T t) {
        this.array.remove(t);
        this.notifyDataSetChanged();
    }

    @Override
    public void remove(int position) {
        this.removeItem(position);
        this.notifyDataSetChanged();
    }

    @Override
    public void remove(List<T> list) {
        if (Util.isEmpty(list))
            return;
        this.array.removeAll(list);
        this.notifyDataSetChanged();
    }

    @Override
    public void replace(T t, int position) {
        this.replaceItem(t, position);
        this.notifyDataSetChanged();
    }

    @Override
    public void replaceCurrents(List<Current<T>> list) {
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
        this.array.clear();
        this.notifyDataSetChanged();
    }

    private void setItems(List<T> list) {
        if (this.array == list)
            return;
        this.array.clear();
        if (Util.isEmpty(list))
            return;
        this.array.addAll(list);
    }

    public boolean addItem(T t, int position) {
        if (addLast(position))
            return array.add(t);

        array.add(position, t);
        return true;
    }

    private boolean addItems(List<T> items, int position) {
        if (Util.isEmpty(items))
            return false;
        if (addLast(position))
            return this.array.addAll(items);

        return this.array.addAll(position, items);
    }

    private boolean addLast(int position) {
        return position == ADD_END || position < 0 || position >= getItemCount();
    }

    private void replaceItem(T t, int position) {
        this.removeItem(position);
        this.addItem(t, position);
    }

    private T removeItem(int position) {
        return this.array.remove(position);
    }

    @Override
    public int getItemViewType(int position) {
        return itemLoading.getItemViewType(getItem(position), position);
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(itemLoading.onCreateView(parent, viewType), this);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        holder.bindPosition(position);
        itemLoading.onLoadView(holder.itemView, holder.getItemViewType(), getItem(position), position);
    }

    @Override
    public int getItemCount() {
        return array == null ? 0 : array.size();
    }

    public T getItem(int position) {
        if (Util.isEmpty(array) || position > array.size() - 1) {
            return null;
        }
        return array.get(position);
    }

    @Override
    public void onClick(View v) {
        if (itemClickListener == null)
            return;
        int position = (int) v.getTag();
        T entity = getItem(position);
        itemClickListener.onItemClick(v, getItemViewType(position), entity, position);
    }

    public static abstract class OnItemLoading<E> {
        public int getItemViewType(E entity, int position) {
            return 0;
        }
        public abstract View onCreateView(ViewGroup parent, int viewType);
        public abstract void onLoadView(View root, int viewType, E entity, int position);
    }

    public interface OnItemClickListener<E> {
        /**
         * Callback method to be invoked when an item in this AdapterView has
         * been clicked.
         * <p/>
         * Implementers can call getItemAtPosition(position) if they need
         * to access the data associated with the selected item.
         *
         * @param view     The view within the AdapterView that was clicked (this
         *                 will be a view provided by the adapter)Ø
         * @param entity   The entity within the adapter data that was clicked item
         * @param position The position of the view in the adapter.
         */
        void onItemClick(View view, int viewType, E entity, int position);
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        public RecyclerViewHolder(View view, View.OnClickListener listener) {
            super(view);
            itemView.setOnClickListener(listener);
        }

        public void bindPosition(int position) {
            itemView.setTag(position);
        }
    }
}
