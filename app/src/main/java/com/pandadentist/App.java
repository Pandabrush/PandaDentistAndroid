package com.pandadentist;

import android.app.Application;
import android.content.Context;

import com.pandadentist.bleconnection.utils.Toasts;
import com.pandadentist.download.DownloadConfiguration;
import com.pandadentist.download.DownloadManager;
import com.pandadentist.log.RunTimeLog;
import com.pandadentist.util.DensityUtil;
import com.pandadentist.bleconnection.utils.Logger;
import com.pandadentist.bleconnection.utils.Util;
import com.tencent.smtt.sdk.QbSdk;
import com.umeng.analytics.MobclickAgent;

/**
 *
 */
public class App extends Application {

    public static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.setLevel(Logger.LEVEL_VERBOSE);

        RunTimeLog.getInstance(this);
        this.initUmeng();
        sContext = this;
        Toasts.register(this);
        DensityUtil.register(this);
        initDownloader();
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Logger.d(" onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), cb);
    }

    private void initUmeng() {
        MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(this.getApplicationContext(), Util.getMetaDataString(this, "UMENG_APPKEY"), Util.getMetaDataString(this, "UMENG_CHANNEL")));
        MobclickAgent.setSessionContinueMillis(600000);//10 * 60 * 1000 = 600000 10分钟
        MobclickAgent.setCatchUncaughtExceptions(true);
    }

    public static Context getContext() {
        return sContext;
    }

    private void initDownloader() {
        DownloadConfiguration configuration = new DownloadConfiguration();
        configuration.setMaxThreadNum(10);
        configuration.setThreadNum(3);
        DownloadManager.getInstance().init(getApplicationContext(), configuration);
    }
}
