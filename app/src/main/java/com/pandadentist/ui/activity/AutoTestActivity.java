package com.pandadentist.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pandadentist.R;
import com.pandadentist.listener.OnAutoTestListener;
import com.pandadentist.log.RunTimeLog;
import com.pandadentist.util.BLEProtoProcess;
import com.pandadentist.bleconnection.utils.Logger;
import com.pandadentist.util.TimeUtil;
import com.pandadentist.widget.TopBar;
import com.pandadentist.widget.recycler.RecyclerAdapter;
import com.pandadentist.widget.recycler.WRecyclerView;

import java.util.Locale;

/**
 * Created by zhangwy on 2018/1/7.
 * Updated by zhangwy on 2018/1/7.
 * Description 自动化测试
 */

public class AutoTestActivity extends BaseActivity implements OnAutoTestListener, RunTimeLog.OnRunTimeLogListener {

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
    private WRecyclerView<RunTimeLog.RunTimeLogItem> recyclerView;
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
        this.recyclerView = (WRecyclerView<RunTimeLog.RunTimeLogItem>) findViewById(R.id.auto_test_recycle);
        Bundle intent = getIntent().getExtras();
        if (!intent.getBoolean(EXTRA_BLT_CONNECT)) {
            this.initNoDevice();
        } else {
            this.initNone();
        }
        this.recyclerView.setKeepScreenOn(true);
    }

    private void initNoDevice() {
        this.recyclerView.setVisibility(View.GONE);
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
        this.tipHome.setVisibility(View.GONE);
        this.recyclerView.setVisibility(View.VISIBLE);
        this.recyclerView.setLinearLayoutManager(WRecyclerView.VERTICAL, false);
        this.recyclerView.loadData(null, new RecyclerAdapter.OnItemLoading<RunTimeLog.RunTimeLogItem>() {
            @Override
            public View onCreateView(ViewGroup parent, int viewType) {
                return LayoutInflater.from(AutoTestActivity.this).inflate(R.layout.item_auto_test, parent, false);
            }

            @Override
            public void onLoadView(View root, int viewType, RunTimeLog.RunTimeLogItem entity, int position) {
                TextView first = (TextView) root.findViewById(R.id.auto_test_first);
                TextView second = (TextView) root.findViewById(R.id.auto_test_second);
                TextView third = (TextView) root.findViewById(R.id.auto_test_third);
                first.setText(getString(R.string.auto_test_msg, entity.action.desc, entity.result.desc, String.format(Locale.getDefault(), "%.2f", entity.useTime / (float) 1000.0)));
                second.setText(TimeUtil.dateMilliSecond2String(entity.nowTime, TimeUtil.PATTERN_DATE));
                third.setText(String.valueOf(entity.content));
            }
        });
        RunTimeLog.getInstance(this).register(this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            this.recyclerView.setKeepScreenOn(false);
        } catch (Exception e) {
            Logger.d("setKeepScreenOn", e);
        }
        RunTimeLog.getInstance(this).unRegister(this);
    }

    @Override
    public void onLog(RunTimeLog.RunTimeLogItem item) {
        this.recyclerView.add(item);
    }
}
