package com.pandadentist.ui.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

    public void todoSearch(String keyword){}

    public abstract int providerLayoutId();

    protected void hideFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        transaction.hide(fragment);
        transaction.commitAllowingStateLoss();
    }

    protected void addFragment(int layoutId, Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        transaction.add(layoutId, fragment);
        transaction.commitAllowingStateLoss();
    }


    protected void addFragment(int layoutId, Fragment fragment) {
        addFragment(layoutId, fragment, false);
    }

    protected void replaceFragment(int layoutId, Fragment fragment) {
        replaceFragment(layoutId, fragment);
    }

    protected void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
//        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.show(fragment);
        transaction.commitAllowingStateLoss();
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

    protected final void registerReceiver(String[] actionArray) {
        if (actionArray == null) {
            return;
        }
        IntentFilter intentfilter = new IntentFilter();
        for (String action : actionArray) {
            intentfilter.addAction(action);
        }
        if (actionArray.length > 0) {
            mReceiver = new BaseBroadcastReceiver();
            registerReceiver(mReceiver, intentfilter);
        }
    }


}
