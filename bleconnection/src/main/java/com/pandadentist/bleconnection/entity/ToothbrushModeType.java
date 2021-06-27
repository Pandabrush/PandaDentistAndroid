package com.pandadentist.bleconnection.entity;

/**
 * CreateTime 2021/6/27 12:10
 * Author zhangwy
 * desc:
 * 设置电机信息
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
public enum ToothbrushModeType {
    MODE(0, "设置牙刷模式"),
    TCLK(1, "设定牙刷周期"),
    PWM(2, "设定牙刷振幅"),
    TIME(3, "设定刷牙时间"),
    ALL(4, "同时设置周期、振幅、时间"),
    VALUE(0xFF, "获取当前设置值");
    public int code;
    public String desc;

    ToothbrushModeType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ToothbrushModeType find(int code) {
        for (ToothbrushModeType type : ToothbrushModeType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        return MODE;
    }
}
