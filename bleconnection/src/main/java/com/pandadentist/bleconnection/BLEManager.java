package com.pandadentist.bleconnection;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.pandadentist.bleconnection.entity.RunTimeEntity;
import com.pandadentist.bleconnection.entity.ToothbrushInfoEntity;
import com.pandadentist.bleconnection.entity.ToothbrushModeType;
import com.pandadentist.bleconnection.entity.ToothbrushSettingConfigEntity;
import com.pandadentist.bleconnection.entity.ToothbrushSettingEntity;
import com.pandadentist.bleconnection.entity.ToothbrushEntity;

import java.util.List;

/**
 * CreateTime 2021/6/20 11:12
 * Author zhangwy
 * desc:
 * 蓝牙管理
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
@SuppressWarnings({"unused", "DanglingJavadoc"})
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

    public abstract void initialize(Context context);

    public abstract void destroy();

    public abstract boolean isEnabled();

    public abstract void scan(OnScanListener listener);

    public abstract void stopScan();

    public abstract void setConnectListener(OnConnectListener listener);

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

    /****************************************以下为交互**********************************************/
    /**
     * 设置蓝牙数据获取回调
     *
     * @param listener 回调接口
     */
    public abstract void setToothbrushListener(OnToothbrushDataListener listener);

    public abstract void reqData(String deviceId);

    public abstract void setToothbrushSettingInfoListener(OnToothbrushSettingInfoListener listener);

    public abstract void getToothbrushSettingConfig(String deviceId, OnToothbrushSettingConfigListener listener);

    public abstract void getToothbrushSettingInfo(String deviceId, OnToothbrushSettingInfoListener listener);

    public abstract void getToothbrushSettingInfo(String deviceId);

    public abstract void setToothbrush(String deviceId, ToothbrushModeType modeType, int mode, int pwm, int tclk, int time);

    public abstract void setErrorAlert(String deviceId, boolean bash, boolean gb, OnToothbrushSetErrorAlertListener listener);

    public abstract void settingFinish(String deviceId);

    public abstract void startRuntime(String deviceId, OnToothbrushRuntimeListener listener);

    public abstract void stopRuntime(String deviceId);

    public abstract void adjusting(String deviceId);

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

    public interface OnConnectListener {
        void onConnected(String deviceId);

        void onDisConnected(String deviceId);

        void onConnectError(String deviceId, int errorCode);
    }

    public interface OnToothbrushDataListener {
        void onReadStart(String deviceId);

        void onData(String deviceId, ToothbrushInfoEntity infoEntity, ToothbrushEntity dataEntity);

        void onNoData(String deviceId);
    }

    public interface OnToothbrushSettingConfigListener {
        /**
         * 牙刷设置配置信息
         *
         * @param deviceId 牙刷地址
         * @param entity   设置配置对象
         */
        void onToothbrushSettingConfig(String deviceId, ToothbrushSettingConfigEntity entity);
    }

    public interface OnToothbrushSettingInfoListener {

        /**
         * 牙刷设置信息
         *
         * @param deviceId 牙刷地址
         * @param entity   牙刷设置对象
         */
        void onToothbrushSettingInfo(String deviceId, ToothbrushSettingEntity entity);
    }

    public interface OnToothbrushSetErrorAlertListener {
        /**
         * 设置完errorAlert的结果
         *
         * @param deviceId 牙刷蓝牙地址
         * @param bash     bash
         * @param gb       gb
         */
        void onErrorAlertValue(String deviceId, boolean bash, boolean gb);
    }

    public interface OnToothbrushRuntimeListener {
        void onToothbrushRuntime(String deviceId, RunTimeEntity entity);
    }
}