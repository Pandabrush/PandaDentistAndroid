package com.pandadentist.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.pandadentist.R;

import butterknife.OnClick;

/**
 * Created by Ford on 2017/5/25.
 * Updated by zhangwy on 2017/11/12
 */

public class ConnectWifiActivity extends SwipeRefreshBaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.hasTopBar()) {
            this.topBar.setLeftVisibility(true);
            this.setOnLeftClickListener();
            this.topBar.setCentreText(R.string.connectWifi);
            this.topBar.setRightVisibility(false);
        }
    }

    @Override
    public int providerLayoutId() {
        return 0;
//        return R.layout.activity_connect_wifi;
    }


    @OnClick(R.id.btn)
    public void onClick() {
        Intent intent = new Intent(ConnectWifiActivity.this,LoadingActivity.class);
        startActivity(intent);
    }
}
