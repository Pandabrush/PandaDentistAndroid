package com.pandadentist.bleconnection.interaction;

/**
 * CreateTime 2021/6/27 18:06
 * Author zhangwy
 * desc:
 * 执行命令
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
public enum Command {
    REQ_DATA(0, Send2Buffer.dataReq(), "获取刷牙数据"),
    TB_INFO(1, Send2Buffer.deviceInfo(), "获取牙刷信息"),
    SETTING_CONFIG(2, Send2Buffer.settingConfig(), "获取牙刷设置配置信息"),
    SETTING_VALUE(3, Send2Buffer.settingValue(), "获取牙刷设置信息"),
    RUNTIME(4, Send2Buffer.runtime(), "实时数据请求"),
    CALLBACK(5, Send2Buffer.callBack(), "返回界面设置（对外设置需要，runtime时stop接口实现）"),
    ADJUSTING(6, Send2Buffer.adjusting(), "牙刷校验设置"),
    ERROR_ALERT(7, Send2Buffer.errAlert(true, true), "错误报警设置");

    public int code;
    public byte[] command;
    public String desc;

    Command(int code, byte[] command, String desc) {
        this.code = code;
        this.command = command;
        this.desc = desc;
    }

    public Command errorAlert(boolean bash, boolean gb) {
        if (this == ERROR_ALERT) {
            this.command = Send2Buffer.errAlert(bash, gb);
        }
        return this;
    }
}