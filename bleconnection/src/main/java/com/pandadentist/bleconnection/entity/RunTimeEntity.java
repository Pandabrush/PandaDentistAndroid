package com.pandadentist.bleconnection.entity;

/**
 * CreateTime 2021/6/27 18:17
 * Author zhangwy
 * desc:
 * TODO
 * -------------------------------------------------------------------------------------------------
 * use:
 * TODO
 **/
public class RunTimeEntity extends BaseEntity {
    public float[] rt = new float[4];
    public int rtIndex = -1;
    public boolean rtPressOk, rtRange, rtAngle;
}
