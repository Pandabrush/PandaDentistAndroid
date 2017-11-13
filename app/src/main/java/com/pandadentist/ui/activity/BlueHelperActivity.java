package com.pandadentist.ui.activity;

import android.os.Bundle;

import com.pandadentist.R;

/**
 * Created by fudaye on 2017/8/29.
 * Updated by zhangwy on 2017/11/12
 */

public class BlueHelperActivity  extends SwipeRefreshBaseActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.hasTopBar()) {
            this.topBar.setLeftVisibility(true);
            this.setOnLeftClickListener();
            this.topBar.setRightVisibility(false);
        }
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_blue_helper;
    }
}
