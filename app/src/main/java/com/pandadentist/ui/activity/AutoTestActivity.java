package com.pandadentist.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pandadentist.R;
import com.pandadentist.listener.OnAutoTestListener;
import com.pandadentist.service.UartService;
import com.pandadentist.util.BLEProtoProcess;
import com.pandadentist.util.Logger;
import com.pandadentist.widget.TopBar;

/**
 * Created by zhangwy on 2018/1/7.
 * Updated by zhangwy on 2018/1/7.
 * Description 自动化测试
 */

public class AutoTestActivity extends BaseActivity implements OnAutoTestListener {

    private static final String EXTRA_HAS_DEVICE = "extraHasDevice";
    private static final String EXTRA_BLT_CONNECT = "extraBltConnect";

    public static void start(Context context, boolean hasDevice, boolean isBltConnect) {
        Intent intent = new Intent(context, AutoTestActivity.class);
        intent.putExtra(EXTRA_HAS_DEVICE, hasDevice);
        intent.putExtra(EXTRA_BLT_CONNECT, isBltConnect);
        context.startActivity(intent);
    }

    private View tipHome;
    private ImageView tipImage;
    private TextView tipText;
    private Button tipButton;
    private View content;
    private BLEProtoProcess protoProcess;
    private boolean openAutoTest = true;
    private Runnable outTime;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.hasTopBar()) {
            this.topBar.setLeftVisibility(true);
            this.topBar.setRightVisibility(false);
            this.topBar.setCentreText(R.string.title_auto_test);
            this.topBar.setCentreTextColor(getResources().getColor(R.color.font_color_toothbrush_default));
            this.topBar.setOnLeftClickListener(new TopBar.OnClickListener() {
                @Override
                public void onClick() {
                    autoTestSetting(false);
                }
            });
        }

        this.tipHome = findViewById(R.id.auto_test_tip);
        this.tipImage = (ImageView) findViewById(R.id.auto_test_tip_image);
        this.tipText = (TextView) findViewById(R.id.auto_test_tip_remind);
        this.tipButton = (Button) findViewById(R.id.auto_test_tip_button);
        this.content = findViewById(R.id.auto_test_scrollView);
        Bundle intent = getIntent().getExtras();
        if (!intent.getBoolean(EXTRA_BLT_CONNECT)) {
            this.initNoDevice();
        } else {
            this.initNone();
        }
        this.content.setKeepScreenOn(true);
    }

    private void initNoDevice() {
        this.content.setVisibility(View.GONE);
        this.tipHome.setVisibility(View.VISIBLE);
        this.tipImage.setImageResource(R.drawable.icon_no_device);
        this.tipText.setText(R.string.msg_unconnect_device);
        this.tipButton.setText(R.string.goto_connect_device);
        this.tipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initNone() {
        this.autoTestSetting(true);
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_auto_test;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            autoTestSetting(false);
            return true;
        }
        return false;
    }

    private void autoTestSetting(final boolean enable) {
        this.protoProcess = this.protoProcess != null ? this.protoProcess : UrlDetailActivity.bleProtoProcess == null ? new BLEProtoProcess() : UrlDetailActivity.bleProtoProcess;
        this.protoProcess.setOnAutoTestListener(this);
        this.openAutoTest = enable;
        if (UrlDetailActivity.mService != null) {
            UrlDetailActivity.mService.writeRXCharacteristic(this.protoProcess.autoRestartMode(enable));
            this.outTime = new Runnable() {
                @Override
                public void run() {
                    showMessage(enable ? R.string.auto_test_setting_open_failed : R.string.auto_test_setting_close_failed);
                }
            };
            postDelayed(this.outTime, 1000);
        } else {
            showMessage("蓝牙服务为空，请杀掉应用重试");
        }
        if (!enable) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 2000);
        }
    }

    @Override
    public void onState(boolean enable) {
        removeCallbacks(this.outTime);
        if (this.openAutoTest && enable) {
            showMessage(R.string.auto_test_setting_open_success);
        } else if (!this.openAutoTest && !enable) {
            showMessage(R.string.auto_test_setting_close_success);
        }
    }

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            if (isDestroyed()) {
                try {
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                } catch (Exception e) {
                    Logger.d("unregisterReceiver", e);
                }
                return;
            }
            switch (intent.getAction()) {
                case UartService.ACTION_GATT_CONNECTED: {
                    break;
                }
                case UartService.ACTION_GATT_DISCONNECTED: {
                    break;
                }
                case UartService.ACTION_GATT_SERVICES_DISCOVERED: {
                    break;
                }
                case UartService.ACTION_DATA_AVAILABLE: {
                    break;
                }
                case UartService.DEVICE_DOES_NOT_SUPPORT_UART: {
                    break;
                }
                case UartService.DEVICE_REFRESH_FALG: {
                    break;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            this.content.setKeepScreenOn(false);
        } catch (Exception e) {
            Logger.d("setKeepScreenOn", e);
        }
    }
}
