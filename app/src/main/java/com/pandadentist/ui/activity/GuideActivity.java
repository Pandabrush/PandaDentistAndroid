package com.pandadentist.ui.activity;

import android.os.Bundle;

import com.pandadentist.R;

import butterknife.OnClick;

/**
 * Created by fudaye on 2017/6/15.
 * Updated by zhangwy on 2017/11/12
 * 设置指示灯
 */

public class GuideActivity extends SwipeRefreshBaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.hasTopBar()) {
            this.topBar.setLeftVisibility(true);
            this.setOnLeftClickListener();
            this.topBar.setCentreText(R.string.help_set_indicator_light);
            this.topBar.setRightVisibility(false);
        }
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_guide;
    }

    @OnClick(R.id.btn)
    public void onViewClicked() {
        finish();
    }
}
