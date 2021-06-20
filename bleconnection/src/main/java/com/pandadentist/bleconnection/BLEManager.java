package com.pandadentist.bleconnection;

import android.content.Context;

/**
 * CreateTime 2021/6/20 11:12
 * Author zhangwy
 * desc:
 * 蓝牙管理
 * -------------------------------------------------------------------------------------------------
 * use:
 *
 **/
public abstract class BLEManager {
    private static BLEManager instance;
    public static BLEManager getInstance(Context context) {
        if (instance == null) {
            synchronized (BLEManager.class) {
                if (instance == null) {
                    //TODO
                }
            }
        }
        return instance;
    }

    public abstract void scan();

    public abstract void binding(String deviceId);

    public abstract void connect();
}