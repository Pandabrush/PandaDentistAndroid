package com.pandadentist.listener;

import android.view.View;

/**
 * Created by Ford on 2016/6/1 0001.
 */
public interface OnItemClickListener<T> {
    void onItemClick(View v, T entity, int position, int code);
}
