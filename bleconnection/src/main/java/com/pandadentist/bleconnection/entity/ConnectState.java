package com.pandadentist.bleconnection.entity;

/**
 * CreateTime 2021/6/20 19:38
 * Author zhangwy
 * desc:
 * TODO
 * -------------------------------------------------------------------------------------------------
 * use:
 * TODO
 **/
public enum ConnectState {
    UNKNOWN(-1, "未知状态"),
    DISCONNECTED(0, "未连接"),
    CONNECTING(2, "连接中"),
    CONNECTED(2, "已连接");
    public final int code;
    public final String desc;

    ConnectState(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ConnectState findByCode(int code) {
        for (ConnectState state : ConnectState.values()) {
            if (state.code == code) {
                return state;
            }
        }
        return UNKNOWN;
    }
}
