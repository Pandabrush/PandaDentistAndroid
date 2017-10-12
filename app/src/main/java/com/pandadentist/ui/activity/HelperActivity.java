package com.pandadentist.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.pandadentist.util.FileUtil;
import com.pandadentist.util.Logger;

import org.json.JSONArray;

import java.util.List;
import java.util.Locale;

/**
 * Created by zhangwy on 2017/9/16.
 * Updated by zhangwy on 2017/9/16.
 */

public class HelperActivity extends Activity implements Handler.Callback{

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
    private OnStateListener onStateListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper);
        this.init();
    }

    private void init() {
        this.nativeHome = findViewById(R.id.helper_native);
        this.nativeImage = (ImageView) findViewById(R.id.helper_native_image);
        this.nativeText = (TextView) findViewById(R.id.helper_native_remind);
        this.nativeButton = (Button) findViewById(R.id.helper_native_button);
        this.webView = (WebView) findViewById(R.id.helper_webView);
        this.webView.setVisibility(View.GONE);
        this.nativeHome.setVisibility(View.GONE);
        Bundle intent = getIntent().getExtras();
//        if (!intent.getBoolean(EXTRA_HAS_DEVICE)) {
//            this.initNoDevice();
//        } else if (!intent.getBoolean(EXTRA_BLT_CONNECT)) {
//            this.initDeviceOn();
//        } else {
            this.initNone();
//        }
    }

    private void initNoDevice() {
        this.nativeHome.setVisibility(View.VISIBLE);
        this.nativeImage.setImageResource(R.drawable.icon_no_device);
        this.nativeText.setText("APP未连接到牙刷，请进行连接后重拾，谢谢配合");
        this.nativeButton.setText("去连接牙刷");
        this.nativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initDeviceOn() {
        this.nativeHome.setVisibility(View.VISIBLE);
        this.nativeImage.setImageResource(R.drawable.icon_device_on);
        this.nativeText.setText("您的牙刷处于开机状态，请关机重启后重拾，谢谢配合");
        this.nativeButton.setText("已关机重新连接蓝牙");
        this.nativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initNone() {
        this.webView.setVisibility(View.VISIBLE);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        this.webView.getSettings().setLoadWithOverviewMode(true);
        this.webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        this.webView.getSettings().setDomStorageEnabled(true);
        this.webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        this.webView.getSettings().setBlockNetworkImage(false);
        // 不保存表单数据
        this.webView.getSettings().setSaveFormData(false);
        // 不保存密码
        this.webView.getSettings().setSavePassword(false);
        // 支持页面放大功能
        this.webView.getSettings().setSupportZoom(true);
        this.webView.setInitialScale(100);

        // 设置webview自适应屏幕大小
        this.webView.getSettings().setUseWideViewPort(true);

        // 不显示页面拖动条
//		this.webView.setHorizontalScrollBarEnabled(false);
//		this.webView.setVerticalScrollBarEnabled(false);
        this.webView.getSettings().setDefaultTextEncodingName("utf-8");
        // this.webView.addJavascriptInterface(new JSObject(), "FS");
        // 设置监听
        // 设置WebViewClient对象
        // 显示页面
        this.webView.loadUrl("http://www.easylinkage.cn/webapp/shuaya/app_index.html");
        this.syncAmin();
    }

    private void syncAmin() {

        BLEProtoProcess bleProtoProcess = UrlDetailActivity.bleProtoProcess == null ? new BLEProtoProcess() : UrlDetailActivity.bleProtoProcess;
        bleProtoProcess.setOnZhenListener(new OnZhenListener() {
            @Override
            public void onZhen(int zhen, int total) {
//                float fz = zhen;
//                float ft = total;
//                float percent = fz / ft * 100f;
//                int ip = (int) percent;
            }
        });
        onStateListener = new OnStateListener() {
            @Override
            public void onRuntimeAct(int state) {
                switch (state) {
                    case 0:
                        break;
                    case 1:
                        Toast.makeText(HelperActivity.this, "请关机后重拾", Toast.LENGTH_LONG).show();
                        break;
                    case 4:
                        Toast.makeText(HelperActivity.this, "设备正忙，请稍候重拾", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Toast.makeText(HelperActivity.this, "请关机后重拾", Toast.LENGTH_LONG).show();
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
                    jsonArray.put(valArray);
                    jsonArray.put(xyzArray);
                    jsonArray.put(state[0]);
                    jsonArray.put(state[1]);
                    jsonArray.put(state[2]);

//                    String info = "javascript:startReplayAnimate([%f, %f, %f, %f], [%f, %f, %f], %b, %b, %b)";
                    String info = "javascript:startReplayAnimate(%s)";
                    String js = String.format(Locale.getDefault(), info, jsonArray.toString());
                    Log.d("Helper", js);
                    webView.loadUrl(js);
                } catch (Exception e) {
                    Logger.e("onRuntime", e);
                }
            }
        };
        bleProtoProcess.setOnStateListener(onStateListener);
        startAnim();
//        UrlDetailActivity.mService.writeRXCharacteristic(bleProtoProcess.getRequests((byte) 20, (byte) 0));
    }

    private Handler handler = new Handler(this);
    private List<FileUtil.Elements> elements;
    private List<FileUtil.CoordInate> coordInates;
    private final int WHAT_ANIM = 100;
    private final int DELAYED_ANIM = 25;
    private int position = 0;

    @Override
    public boolean handleMessage(Message msg) {
        if (position < elements.size() && position < coordInates.size()) {
            FileUtil.Elements element = elements.get(position);
            FileUtil.CoordInate coordInate = coordInates.get(position);
            if (element != null && coordInate != null) {
                float[] val = {element.getFirst(), element.getSecond(), element.getThird(), element.getFour()};
                float[] xyz = {coordInate.getX(), coordInate.getY(), coordInate.getZ()};
                boolean[] state = {true, false, false};
                onStateListener.onRuntime(val, xyz, state);
            }
            position++;
            sendAnim();
        }
        return true;
    }

    private void startAnim() {
        elements = FileUtil.readElementsFromAsset(this);
        coordInates = FileUtil.readCoordInateFromAsset(this);
        if (elements == null || elements.isEmpty() || coordInates == null || coordInates.isEmpty()) {
            Toast.makeText(this, "无数据", Toast.LENGTH_LONG).show();
            return;
        }
        this.sendAnim();
    }

    private void sendAnim() {
        if (position >= elements.size() || position >= coordInates.size()) {
            Toast.makeText(this, "动画结束", Toast.LENGTH_LONG).show();
            return;
        }
        handler.sendEmptyMessageDelayed(WHAT_ANIM, DELAYED_ANIM);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeMessages(WHAT_ANIM);
        }
    }
}
