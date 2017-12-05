/*************************************************************************************
 * Module Name: 具体模块见相应注释
 * File Name: FSAdapterRefreshData.java
 * Description: 
 * Author: 张维亚
 * All Rights Reserved
 * 所有版权保护
 * Copyright 2014, Funshion Online Technologies Ltd.
 * 版权 2014，北京风行在线技术有限公司
 * This is UNPUBLISHED PROPRIETARY SOURCE CODE of Funshion Online Technologies Ltd.;
 * the contents of this file may not be disclosed to third parties, copied or
 * duplicated in any form, in whole or in part, without the prior written
 * permission of Funshion Online Technologies Ltd.
 * 这是北京风行在线技术有限公司未公开的私有源代码。本文件及相关内容未经风行在线技术有
 * 限公司事先书面同意，不允许向任何第三方透露，泄密部分或全部; 也不允许任何形式的私自备份。
 ***************************************************************************************/
package com.pandadentist.widget;

import java.util.List;

/**
 * Author: 张维亚
 * 创建时间：2014年7月5日 上午10:23:06
 * 修改时间：2014年7月5日 上午10:23:06
 * Description: 刷新适配器data的接口
 **/
@SuppressWarnings("unused")
public interface ZRefreshAdapterCallBack<T> {

    void add(T t);

    void add(T t, int position);

    void addAll(List<T> list);

    void addAll(List<T> list, int position);

    void addCurrentAll(List<Current<T>> list);

    void remove(T t);

    void remove(int position);

    void removeAll(List<T> list);

    void replace(T t, int position);

    void replaceCurrentAll(List<Current<T>> list);

    void reload(List<T> list);

    void clear();

    public static class Current<T> {
        public int position;
        public T t;
    }
}