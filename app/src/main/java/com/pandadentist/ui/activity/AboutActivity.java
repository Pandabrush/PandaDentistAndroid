package com.pandadentist.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.pandadentist.R;
import com.pandadentist.util.Device;
import com.pandadentist.widget.TopBar;

/**
 * Created by zhangwy on 2017/12/24.
 * Updated by zhangwy on 2017/12/24.
 * Description
 */

public class AboutActivity extends BaseActivity {

    public static void start(Context context) {
        context.startActivity(new Intent(context, AboutActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.hasTopBar()) {
            this.topBar.setLeftVisibility(true);
            this.topBar.setRightVisibility(false);
            this.topBar.setOnLeftClickListener(new TopBar.OnClickListener() {
                @Override
                public void onClick() {
                    finish();
                }
            });
            this.topBar.setCentreText(R.string.title_about);
            this.topBar.setCentreTextColor(getResources().getColor(R.color.font_color_toothbrush_default));
        }
        TextView version = (TextView) findViewById(R.id.version);
        version.setText(Device.App.getVersionName(this));
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_about;
    }
}
