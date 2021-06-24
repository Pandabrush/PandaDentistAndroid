package com.pandadentist.bleconnection;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.util.List;

/**
 * CreateTime 2021/6/20 11:12
 * Author zhangwy
 * desc:
 * 蓝牙管理
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
@SuppressWarnings("unused")
public abstract class BLEManager {
    private static BLEManager instance;

    public static BLEManager getInstance() {
        if (instance == null) {
            synchronized (BLEManager.class) {
                if (instance == null) {
                    instance = new BLEManagerImpl();
                }
            }
        }
        return instance;
    }

    public abstract void create(Context context);

    public abstract void destroy();

    public abstract boolean isEnabled();

    public abstract void scan(OnScanListener listener);

    public abstract void stopScan();

    /**
     * 连接设备默认为增量连接
     *
     * @param deviceId 蓝牙设备deviceId
     */
    public abstract boolean connect(String deviceId);

    /**
     * 连接设备
     *
     * @param deviceId 蓝牙设备deviceId
     * @param increase true为增量连接，false为断开之前的连接后在连接现在的连接
     */
    public abstract boolean connect(String deviceId, boolean increase);

    /**
     * 连接多台设备默认为增量连接
     *
     * @param devicesId 多台蓝牙设备deviceId列表
     */
    public abstract void connect(List<String> devicesId);

    /**
     * 连接多台设备默认为增量连接
     *
     * @param devicesId 多台蓝牙设备deviceId列表
     * @param increase  true为增量连接，false为断开之前的连接后在连接现在的连接
     */
    public abstract void connect(List<String> devicesId, boolean increase);

    /**
     * 断开连接单台设备
     *
     * @param deviceId 蓝牙设备deviceId
     */
    public abstract void disConnect(String deviceId);

    /**
     * 断开连接多台设备
     *
     * @param devicesId 多台蓝牙设备deviceId列表
     */
    public abstract void disConnect(List<String> devicesId);

    /**
     * 断开连接所有设备
     */
    public abstract void disConnectAll();

    public abstract void syncData(String deviceId);

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