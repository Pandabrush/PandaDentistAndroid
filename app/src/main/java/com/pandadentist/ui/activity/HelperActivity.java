package com.pandadentist.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pandadentist.R;
import com.pandadentist.listener.OnStateListener;
import com.pandadentist.listener.OnZhenListener;
import com.pandadentist.util.BLEProtoProcess;
import com.pandadentist.bleconnection.utils.Logger;
import com.pandadentist.widget.TopBar;

import org.json.JSONArray;

import java.util.Locale;

/**
 * Created by zhangwy on 2017/9/16.
 * Updated by zhangwy on 2017/9/16.
 */
@SuppressWarnings("unused")
public class HelperActivity extends BaseActivity {

    private static final String EXTRA_HAS_DEVICE = "extraHasDevice";
    private static final String EXTRA_BLT_CONNECT = "extraBltConnect";

    public static void start(Context context, boolean hasDevice, boolean isBltConnect) {
        Intent intent = new Intent(context, HelperActivity.class);
        intent.putExtra(EXTRA_HAS_DEVICE, hasDevice);
        intent.putExtra(EXTRA_BLT_CONNECT, isBltConnect);
        context.startActivity(intent);
    }

    private View nativeHome;
    private ImageView nativeImage;
    private TextView nativeText;
    private Button nativeButton;
    private WebView webView;
    private BLEProtoProcess bleProtoProcess;

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
            this.topBar.setCentreText(R.string.title_help);
            this.topBar.setCentreTextColor(getResources().getColor(R.color.font_color_toothbrush_default));
        }
        this.init();
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_helper;
    }

    private void init() {
        this.nativeHome = findViewById(R.id.helper_native);
        this.nativeImage = (ImageView) findViewById(R.id.helper_native_image);
        this.nativeText = (TextView) findViewById(R.id.helper_native_remind);
        this.nativeButton = (Button) findViewById(R.id.helper_native_button);
        this.webView = (WebView) findViewById(R.id.helper_webView);
        Bundle intent = getIntent().getExtras();
        if (!intent.getBoolean(EXTRA_BLT_CONNECT)) {
            this.initNoDevice();
        } else {
            this.initNone();
        }
    }

    private void initNoDevice() {
        this.webView.setVisibility(View.GONE);
        this.nativeHome.setVisibility(View.VISIBLE);
        this.nativeImage.setImageResource(R.drawable.icon_no_device);
        this.nativeText.setText(R.string.msg_unconnect_device);
        this.nativeButton.setText(R.string.goto_connect_device);
        this.nativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initDeviceOn() {
        this.webView.setVisibility(View.GONE);
        this.nativeHome.setVisibility(View.VISIBLE);
        this.nativeImage.setImageResource(R.drawable.icon_device_on);
        this.nativeText.setText(R.string.msg_needs_reopen_device);
        this.nativeButton.setText(R.string.has_reopen_connect_device);
        this.nativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initNone() {
        this.nativeHome.setVisibility(View.GONE);
        this.webView.setVisibility(View.VISIBLE);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        this.webView.getSettings().setLoadWithOverviewMode(true);
        this.webView.getSettings().setDomStorageEnabled(true);
        this.webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        this.webView.getSettings().setBlockNetworkImage(false);
        this.webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 不保存表单数据
        this.webView.getSettings().setSaveFormData(false);
        // 支持页面放大功能
        this.webView.getSettings().setSupportZoom(true);
        this.webView.setInitialScale(100);

        // 设置webView自适应屏幕大小
        this.webView.getSettings().setUseWideViewPort(true);

        // 不显示页面拖动条
		this.webView.setHorizontalScrollBarEnabled(false);
		this.webView.setVerticalScrollBarEnabled(false);
        this.webView.getSettings().setDefaultTextEncodingName("utf-8");
        // this.webView.addJavascriptInterface(new JSObject(), "FS");
        // 显示页面
        this.webView.loadUrl("http://www.easylinkage.cn/webapp/shuaya/app_index.html");
        this.syncAnimation();
    }

    private void syncAnimation() {

        this.bleProtoProcess = UrlDetailActivity.bleProtoProcess == null ? new BLEProtoProcess() : UrlDetailActivity.bleProtoProcess;
        this.bleProtoProcess.setOnZhenListener(new OnZhenListener() {
            @Override
            public void onZhen(int zhen, int total) {
            }
        });
        OnStateListener onStateListener = new OnStateListener() {
            @Override
            public void onRuntimeAct(int state) {
                switch (state) {
                    case 0:
                        break;
                    case 1:
                        Toast.makeText(HelperActivity.this, "请关机后重试", Toast.LENGTH_LONG).show();
                        break;
                    case 4:
                        Toast.makeText(HelperActivity.this, "设备正忙，请稍候重试", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Toast.makeText(HelperActivity.this, "请关机后重试", Toast.LENGTH_LONG).show();
                        break;
                }
            }

            @Override
            public void onRuntime(float[] val, float[] xyz, boolean[] state) {
                if (val == null || val.length < 4 | xyz == null || val.length < 3 || state == null || state.length < 3) {
                    Toast.makeText(HelperActivity.this, "设备正忙，请稍候重拾", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    JSONArray valArray = new JSONArray();
                    for (int i = 0; i < 4; i++) {
                        valArray.put(val[i]);
                    }
                    JSONArray xyzArray = new JSONArray();
                    for (int i = 0; i < 3; i++) {
                        xyzArray.put(xyz[i]);
                    }
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(xyzArray);
                    jsonArray.put(valArray);
                    jsonArray.put(state[0]);
                    jsonArray.put(state[1]);
                    jsonArray.put(state[2]);

                    String info = "javascript:startReplayAnimate(%s)";
                    String js = String.format(Locale.getDefault(), info, jsonArray.toString());
                    Log.d("Helper", js);
                    webView.loadUrl(js);
                } catch (Exception e) {
                    Logger.e("onRuntime", e);
                }
            }
        };
        this.bleProtoProcess.setOnStateListener(onStateListener);
        UrlDetailActivity.mService.writeRXCharacteristic(this.bleProtoProcess.getRequests((byte) 20, (byte) 0));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.bleProtoProcess != null && UrlDetailActivity.mService != null) {
            UrlDetailActivity.mService.writeRXCharacteristic(this.bleProtoProcess.quitRunTime());
            this.bleProtoProcess = null;
        }
    }
}
