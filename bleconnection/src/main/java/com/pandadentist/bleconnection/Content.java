package com.pandadentist.bleconnection;

/**
 * CreateTime 2021/6/20 14:14
 * Author zhangwy
 * desc:
 * <p>
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
public class Content {
    public static final int CODE_ERROR_BLE_NONSCAN = -103;//没有扫描
    public static final int CODE_ERROR_BLE_START_SCAN = -104;//启动蓝牙扫描失败
    public static final int CODE_ERROR_BLE_SCANNING = -102;//扫描中
    public static final int CODE_ERROR_BLE_NONSUPPORT = -100;//设备不支持蓝牙
    public static final int CODE_ERROR_BLE_UNOPENED = -101;//设备蓝牙未打开
    public static final int CODE_ERROR_BLE_OPEN = -1;//蓝牙打开错误
    public static final int CODE_ERROR_BLE_CLOSE = -2;//蓝牙关闭错误
    public static final int CODE_ERROR_BLE_SEARCH = -3;//蓝牙扫描中出现错误
    public static final int CODE_ERROR_BLE_SEARCH_STOP = -4;//停止蓝牙扫描出现错误
    public static final int CODE_ERROR_BLE_CONNECT = -5;//蓝牙连接出现错误
    public static final int CODE_ERROR_BLE_DISCONNECT = -6;//断开蓝牙出现错误
    public static final int CODE_ERROR_SEND_DATA = -8;//发送数据出现错误
    public static final int CODE_ERROR_GET_SERVICES = -10;//获取Services出现错误
    public static final int CODE_ERROR_GET_CHARACTORS = -11;//获取Characters出现错误
}
