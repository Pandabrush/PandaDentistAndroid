package com.pandadentist.listener;

/**
 * Created by zhangwy on 2017/9/16.
 */

public interface OnStateListener {
    void onRuntimeAct(int state);
    void onRuntime(float[] val, float[] xyz, boolean[] state);
}
