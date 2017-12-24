package com.pandadentist.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.pandadentist.R;
import com.pandadentist.config.Constants;
import com.pandadentist.entity.WXEntity;
import com.pandadentist.network.APIFactory;
import com.pandadentist.network.APIService;
import com.pandadentist.util.IntentHelper;
import com.pandadentist.util.SPUitl;
import com.pandadentist.util.Toasts;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import butterknife.Bind;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by Ford on 2016/10/14.
 * Updated by zhangwy on 2017/11/12
 * test
 */
public class LoginActivity extends SwipeRefreshBaseActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String APP_ID = "wxa2fe13a5495f3908";
    @Bind(R.id.et_username)
    EditText etUsername;
    @Bind(R.id.et_pwd)
    EditText etPwd;
    private IWXAPI api;
    private CodeReceiverBroadcast broadcast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!TextUtils.isEmpty(SPUitl.getToken())) {
            UrlDetailActivity.start(this);
            finish();
        }
        api = WXAPIFactory.createWXAPI(this, APP_ID);
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        broadcast = new CodeReceiverBroadcast();
        lbm.registerReceiver(broadcast, new IntentFilter(Constants.BROADCAST_FLAG_CODE_MESSAGE));
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_login;
    }

    private void WXLogin() {
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo_test";
        api.sendReq(req);
    }

    private void getToken(String code) {
        APIService api = new APIFactory().create(APIService.class);
        Subscription s = api.getWXToken(code, Constants.AAAA)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<WXEntity>() {
                    @Override
                    public void call(WXEntity wxEntity) {
                        if (wxEntity.getCode() == Constants.SUCCESS) {
                            SPUitl.saveToken(wxEntity.getToken());
                            SPUitl.saveWXUser(wxEntity);
                            UrlDetailActivity.start(LoginActivity.this);
                            finish();
                        } else {
                            Toasts.showShort("绑定失败，请稍后重试");
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d("throwable", "throwable-->" + throwable.toString());
                    }
                });
        addSubscription(s);
    }

    @OnClick({R.id.ll_email_register, R.id.ll_wxlogin, R.id.btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_email_register:
                IntentHelper.gotoEmailRegister(LoginActivity.this);
                break;
            case R.id.ll_wxlogin:
                WXLogin();
                break;
            case R.id.btn:
                // 邮箱登录
                String username = etUsername.getText().toString();
                String pwd = etPwd.getText().toString();
                if (TextUtils.isEmpty(username)) {
                    Toasts.showShort("邮箱不能为空");
                    return;
                }
                if (TextUtils.isEmpty(pwd)) {
                    Toasts.showShort("密码不能为空");
                    return;
                }
                loginForEmail(username, pwd);
//                IntentHelper.gotoMain(LoginActivity.this);
//                IntentHelper.gotoloadingActivity(this,"");
                break;
        }
    }

    class CodeReceiverBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String str = intent.getStringExtra(Constants.BUNDLE_KEY.VALUE);
            Log.d(TAG, "code--->" + str);
            if (!TextUtils.isEmpty(str)) {
                getToken(str);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcast);
    }

    private void loginForEmail(final String username, String pwd) {
        APIService api = new APIFactory().create(APIService.class);
        Subscription s = api.loginForEmail(username, pwd, Constants.AAAA)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<WXEntity>() {
                    @Override
                    public void call(WXEntity wxEntity) {

                        if (Constants.SUCCESS == wxEntity.getCode()) {
                            SPUitl.saveToken(wxEntity.getToken());
                            WXEntity.InfoBean infoBean = wxEntity.getInfo() == null ? new WXEntity.InfoBean() : wxEntity.getInfo();
                            infoBean.setName(username);
                            wxEntity.setInfo(infoBean);
                            SPUitl.saveWXUser(wxEntity);
                            UrlDetailActivity.start(LoginActivity.this);
                            finish();
                        } else if (20002 == wxEntity.getCode()) {
                            Toasts.showShort("用户名不存在");
                        } else if (20003 == wxEntity.getCode()) {
                            Toasts.showShort("用户名或密码错误");
                        } else {
                            Toasts.showShort("登录失败，请检查网络");
                            Log.d(TAG, "错误code ：" + wxEntity.getCode() + "错误信息：" + wxEntity.getMessage());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Toasts.showShort("登录失败，请检查网络");
                        Log.d("throwable", "throwable-->" + throwable.toString());
                    }
                });
        addSubscription(s);
    }
}
