package com.pandadentist.bleconnection;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

/**
 * CreateTime 2021/6/20 11:12
 * Author zhangwy
 * desc:
 * 蓝牙管理
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
public abstract class BLEManager {
    private static BLEManager instance;

    public static BLEManager getInstance(Context context) {
        if (instance == null) {
            synchronized (BLEManager.class) {
                if (instance == null) {
                    instance = new BLEManagerImpl();
                }
            }
        }
        return instance;
    }

    public abstract void scan(OnScanListener listener);

    public abstract void stopScan();

    public abstract void binding(String deviceId);

    public abstract void connect();

    public interface OnScanListener {
        /**
         * 开始扫描
         */
        void onScanStart();

        /**
         * 扫描到设备
         *
         * @param device 扫描到的设备
         */
        void onScanDevice(BluetoothDevice device);

        /**
         * 扫描结束
         */
        void onScanEnd();

        /**
         * 异常错误
         *
         * @param code 错误码
         */
        void onScanError(int code);
    }
}