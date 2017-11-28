package com.pandadentist.listener;

/**
 * Created by zhangwy on 2017/11/26.
 * Updated by zhangwy on 2017/11/26.
 * Description
 */
public interface OnModelChangeListener {
    /**
     * @param modelType 振动类型， 0-按档位设定 1-改变周期 2-改变幅度 3-时间，4-同时改变周期和幅度和时间
     * @param model 振动档位 不同款对应不一样，0-4
     * @param pwm 占空比  范围 200 - 1800
     * @param tClk 周期  范围(2000(高频)->5000(低频)) 默认3800对应(31000次/min)
     * @param time 刷牙时间  单位s
     * @param modelResult 结果
     */
    void onModelChange(int modelType, int model, int pwm, int tClk, int time, int modelResult);
}
