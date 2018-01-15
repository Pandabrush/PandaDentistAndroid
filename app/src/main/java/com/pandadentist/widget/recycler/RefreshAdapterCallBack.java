package com.pandadentist.widget.recycler;

import java.util.List;

/**
 * Author: 张维亚
 * 创建时间：2014年7月5日 上午10:23:06
 * 修改时间：2014年7月5日 上午10:23:06
 * Description: 刷新适配器data的接口
 **/
public interface RefreshAdapterCallBack<T> {

    void add(T t);

    void add(T t, int position);

    void addAll(List<T> list);

    void addAll(List<T> list, int position);

    void addCurrents(List<Current<T>> list);

    void remove(T t);

    void remove(int position);

    void remove(List<T> list);

    void replace(T t, int position);

    void replaceCurrents(List<Current<T>> list);

    void reload(List<T> list);

    void clear();

    class Current<T> {
        public int position;
        public T t;
    }
}