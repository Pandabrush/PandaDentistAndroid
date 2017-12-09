package com.pandadentist.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pandadentist.R;
import com.pandadentist.util.IntentHelper;
import com.pandadentist.util.SPUitl;
import com.pandadentist.widget.RoundProgressBarWidthNumber;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 欢迎界面
 * Updated by zhangwy on 2017/11/12
 */
public class SplashActivity extends SwipeRefreshBaseActivity {

    @Bind(R.id.welcome_iv)
    ImageView welcomeIv;
    @Bind(R.id.prog)
    RoundProgressBarWidthNumber prog;
    @Bind(R.id.rl_skip)
    RelativeLayout mSkip;
    @Bind(R.id.tv)
    TextView tv;

    private int progNum = 0;
    private Timer timer;
    private TimerTask timerTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                progNum += 3;
                prog.setProgress(progNum);
                if (progNum >= 100) {
                    timer.cancel();
                    timerTask.cancel();
                    goMainActivity();
                }
            }
        };
        // 倒计时ji
        timer.schedule(timerTask, 0, 110);
    }

    @Override
    protected boolean fullScreen() {
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_splash;
    }

    private void goMainActivity() {
//        if (SPUitl.firstLaunch(this)) {
//        } else
        if (!TextUtils.isEmpty(SPUitl.getToken())) {
            UrlDetailActivity.start(this);
        } else {
            IntentHelper.gotoLogin(SplashActivity.this);
        }
        finish();
    }

    @OnClick({R.id.rl_skip, R.id.welcome_iv})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_skip:
                timer.cancel();
                timerTask.cancel();
                goMainActivity();
                break;
            case R.id.welcome_iv:
                //点击广告
//                if(mSplash == null ){
//                    return;
//                }
//                timer.cancel();
//                timerTask.cancel();
//                goMainActivity();
//                IntentHelper.gotoDetailActivity(this, mSplash.getSpecialPosConts().getLinkType(), mSplash.getSpecialPosConts().getUrl());
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timerTask.cancel();
        timer = null;
        timerTask = null;
    }
}
