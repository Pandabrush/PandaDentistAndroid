package com.pandadentist.ui.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pandadentist.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public abstract class BaseActivity extends AppCompatActivity {

    @Nullable
    @Bind(R.id.tv_toolbar_title)
    public TextView mToolBarTitle;
    @Nullable
    @Bind(R.id.rl_toolbar_func)
    public RelativeLayout mToolbarFuncRl;
    @Nullable
    @Bind(R.id.iv_toolbar_func)
    public ImageView mToolbarFuncIv;
    @Nullable
    @Bind(R.id.rl_toolbar_back)
    public RelativeLayout mToolBackRl;
    @Nullable
    @Bind(R.id.tv_toolbar)
    public TextView mToolbarFuncTv;
    @Nullable
    @Bind(R.id.ll_toolbar_search)
    public LinearLayout mLLSearch;
    @Nullable
    @Bind(R.id.iv_toolbar_back)
    public ImageView mTitleBackIv;

    @Nullable
    @OnClick(R.id.rl_toolbar_back)
    public void onClickBack() {
        onBackPressed();
    }

    public BaseBroadcastReceiver mReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(providerLayoutId());
        ButterKnife.bind(this);
    }

    public abstract int providerLayoutId();

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
}
