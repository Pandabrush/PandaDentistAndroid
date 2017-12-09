package com.pandadentist.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.pandadentist.R;
import com.pandadentist.util.Logger;
import com.pandadentist.widget.TopBar;
import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;

/**
 * Created by zhangwy on 2017/11/12.
 * Updated by zhangwy on 2017/11/12.
 * Description
 */
@SuppressWarnings("unused")
public abstract class BaseActivity extends AppCompatActivity {
    protected TopBar topBar;
    protected BaseBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.fullScreen()) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        this.setContentView(this.providerLayoutId());
        this.findTopBar();
        ButterKnife.bind(this);
    }

    protected boolean fullScreen() {
        return false;
    }

    public abstract int providerLayoutId();

    protected final void setOnLeftClickListener() {
        if (this.hasTopBar()) {
            this.topBar.setOnLeftClickListener(new TopBar.OnClickListener() {
                @Override
                public void onClick() {
                    onBackPressed();
                }
            });
        }
    }

    protected final boolean hasTopBar() {
        if (this.topBar == null) {
            this.findTopBar();
        }
        return this.topBar != null;
    }

    protected void findTopBar() {
        try {
            View view = this.findViewById(R.id.toolbar_topBar);
            if (view != null) {
                this.topBar = (TopBar) view;
            }
        } catch (Exception e) {
            Logger.e("findTopBar", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    private class BaseBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                handleReceiver(context, intent);
            }
        }
    }

    protected void handleReceiver(Context context, Intent intent) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    protected final void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        Logger.d(msg);
    }

    protected final void showMessage(@StringRes int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
        Logger.d(getString(resId));
    }

    protected final boolean postDelayed(View view, Runnable action, long delayMillis) {
        return view != null && action != null && view.postDelayed(action, delayMillis >= 0 ? delayMillis : 0);
    }
}
