package com.pandadentist.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.pandadentist.BuildConfig;
import com.pandadentist.R;
import com.pandadentist.bleconnection.utils.BLEProtoProcess;
import com.pandadentist.bleconnection.utils.Logger;
import com.pandadentist.bleconnection.utils.RunTimeLog;
import com.pandadentist.bleconnection.utils.Toasts;
import com.pandadentist.bleconnection.utils.Util;
import com.pandadentist.config.Constants;
import com.pandadentist.entity.DeviceListEntity;
import com.pandadentist.entity.WXEntity;
import com.pandadentist.listener.OnItemClickListener;
import com.pandadentist.network.APIFactory;
import com.pandadentist.network.APIService;
import com.pandadentist.service.UartService;
import com.pandadentist.ui.adapter.PopDeviceAdapter;
import com.pandadentist.util.DensityUtil;
import com.pandadentist.util.IntentHelper;
import com.pandadentist.util.SPUitl;
import com.pandadentist.widget.RecycleViewDivider;
import com.pandadentist.widget.TopBar;
import com.pandadentist.widget.X5ObserWebView;
import com.tencent.mm.opensdk.modelbiz.JumpToBizProfile;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.pandadentist.config.Constants.ACTIVITY_FOR_RESULT_REQUEST_CODE_SELECT_DEVICE;

/**
 * zhangwy
 */
@SuppressWarnings({"unused", "Convert2Lambda", "Anonymous2MethodRef", "ConstantConditions", "deprecation", "DuplicateBranchesInSwitch"})
public class UrlDetailActivity extends SwipeRefreshBaseActivity implements NavigationView.OnNavigationItemSelectedListener, Handler.Callback {

    public static void start(Context context) {
        Intent intent = new Intent(context, UrlDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    private static final int REQUEST_ENABLE_BT = 2;

    @Bind(R.id.iv_hint)
    ImageView mivHint;
    @Bind(R.id.wv)
    X5ObserWebView mWebView;
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.rl_content)
    RelativeLayout rlContent;
    @Bind(R.id.rl_guide)
    RelativeLayout rlGuide;
    @Bind(R.id.title)
    View appbar;
    @Bind(R.id.rl_tips)
    RelativeLayout rlTips;
    @Bind(R.id.tv)
    TextView tvUpdateRecord;

    private static final String APP_ID = "wxa2fe13a5495f3908";
    private CircleImageView headerIv;
    private TextView usernameTv;
    private IWXAPI api;
    private PopupWindow mPopupWindow;
    private LinearLayout llSwitchDevice;
    private TextView mTvDeviceName;
    private TextView tvIsConnect;
    public TextView mToolBarTitle;
    private PopupWindow mDevicePop;

    private List<DeviceListEntity.DevicesBean> data = new ArrayList<>();
    public static BLEProtoProcess bleProtoProcess = new BLEProtoProcess();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private int timecount = 0;
    private int runtype = 0;//0-未运行， 1-接收数据过程， 2-核对丢失帧过程
    private int checkCount = 0;
    private String currentMacAddress;
    private boolean isBltConnect = false;

    private Handler handler = new Handler(this);
    private final static int WHAT_CONNECT_BLUETOOTH_TIMEOUT = 100;//连接超时
    private final static int WHAT_RECONNECT_BLUETOOTH = 101;//重连
    private final static int DELAYMILLIS_CONNECT_BLUETOOTH = 15000;
    private final static int DELAYMILLIS_RECONNECT_BLUETOOTH = 15000;

    private long updateTimeStart = -1;
    private long transferTimeStart = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean b = SPUitl.isFirstRun();
        findViewById(R.id.tv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlTips.setVisibility(View.GONE);
            }
        });
        if (this.hasTopBar()) {
            this.topBar.setLeftVisibility(true);
            this.topBar.setLeftImage(R.drawable.ic_main_personal, false);
            this.topBar.setOnLeftClickListener(new TopBar.OnClickListener() {
                @Override
                public void onClick() {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
            this.topBar.setRightImage(R.drawable.ic_add_device, false);
            this.topBar.setOnRightClickListener(new TopBar.OnClickListener() {
                @Override
                public void onClick() {
                    popUpMyOverflow();
                }
            });
            this.topBar.setCentreContent(R.layout.layout_detail_topbar_centre);
            try {
                this.mTvDeviceName = this.findViewById(R.id.tv_device_name);
                this.tvIsConnect = this.findViewById(R.id.tv_isConnect);
                this.mToolBarTitle = this.findViewById(R.id.tv_toolbar_title);
                this.mToolBarTitle.setText(R.string.app_name);
                this.llSwitchDevice = this.findViewById(R.id.ll_switch_device);
                this.llSwitchDevice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (data.size() != 0) {
                            initPopDeviceList(data, true);
                        }
                    }
                });
            } catch (Exception e) {
                Logger.e("addTopBar.CentreContent", e);
            }
        }
        if (b) {
            rlContent.setVisibility(View.GONE);
            rlGuide.setVisibility(View.VISIBLE);
        } else {
            rlContent.setVisibility(View.VISIBLE);
            rlGuide.setVisibility(View.GONE);
        }

        api = WXAPIFactory.createWXAPI(this, APP_ID);
        api.registerApp(APP_ID);
        headerIv = findViewById(R.id.imageView);
        usernameTv = findViewById(R.id.textView3);
        mWebView.setOnScrollChangedCallback(new X5ObserWebView.OnScrollChangedCallback() {
            public void onScroll(int l, int t) {
                Logger.d("We Scrolled etc..." + l + " t =" + t);
                if (mSwipeRefreshLayout != null) {
                    if (t == 0) {//webView在顶部
                        mSwipeRefreshLayout.setEnabled(true);
                    } else {//webView不是顶部
                        mSwipeRefreshLayout.setEnabled(false);
                    }
                }
            }
        });
        //检查权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "该设备不支持蓝牙", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // 打开蓝牙
        if (!mBtAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            service_init();
        }
        loadData();

        this.goneTest();
    }

    private void goneTest() {
        findViewById(R.id.ll_auto_test).setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
    }

    private void loadData() {
        if (SPUitl.isLogin()) {
            mivHint.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
            loadUrl(getUrl(SPUitl.getToken()));
            WXEntity wxEntity = SPUitl.getWXUser();
            if (wxEntity != null) {
                Glide.with(this).load(wxEntity.getInfo().getIcon()).into(headerIv);
                usernameTv.setText(wxEntity.getInfo().getName());
            }
        } else {
            mivHint.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.GONE);
        }
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_main;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    private void loadUrl(String url) {
        setRefresh(true);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onLoadResource(WebView webView, String url) {
                super.onLoadResource(webView, url);
                Logger.d(String.format(Locale.getDefault(), "onLoadResource:%1$s", url));
                if (!TextUtils.isEmpty(url) && url.startsWith("easylinkage://devices.delete")) {
                    final String USER_KEY = "userid";
                    HashMap<String, String> queries = Util.getUrlQueries(url);
                    if (Util.isEmpty(queries) && !queries.containsKey(USER_KEY))
                        return;
                    String userId = queries.get(USER_KEY);
                    if (TextUtils.isEmpty(userId)) {
                        return;
                    }
                    try {
                        DeviceListEntity.DevicesBean devicesBean = findDevice(Integer.parseInt(userId));
                        if (devicesBean != null) {
                            try {
                                data.remove(devicesBean);
                            } catch (Exception e) {
                                Logger.d("", e);
                            }
                            if (mService != null)
                                mService.disconnect();
                            getDeviceList();
                        }
                    } catch (Exception e) {
                        Logger.d("easylinkage://devices.delete", e);
                    }
                }
            }

            @Override
            public void onPageFinished(WebView webView, String url) {
                super.onPageFinished(webView, url);
                Logger.d("onPageFinished.url:" + url);
                setRefresh(false);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Logger.d("shouldOverrideUrlLoading.url:" + url);
                return false;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient());

        WebSettings settings = mWebView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setJavaScriptEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setDomStorageEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);

        mWebView.loadUrl(url);
    }

    @Override
    public void requestDataRefresh() {
        super.requestDataRefresh();
        loadUrl(getUrl(SPUitl.getToken()));
        // 连接设备  传输数据  如果连接了就请求同步数据
        if (isBltConnect) {
            // 直接请求同步
            Logger.d("直接同步数据");
            postDelayedOnUIThread(appbar, new Runnable() {
                @Override
                public void run() {
                    dismiss();
                    if (runtype == 0) {
                        setIsConnectText("正在同步数据中...");
                        if (mService != null)
                            mService.writeRXCharacteristic(bleProtoProcess.getRequests((byte) 1, (byte) 0));
                        bleProtoProcess.setIsreqenddatas(false);
                        bleProtoProcess.setHasrecieved(false);
                    }
                }
            }, 1000);
        } else {
            Logger.d("requestDataRefresh isBltConnect = false");
            if (mDevice != null && mService != null) {
                mService.disconnect();
            }
            if (data.size() > 0) {
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(currentMacAddress);
                Logger.d("currentMacAddress-->" + currentMacAddress);
                postDelayedOnUIThread(appbar, new Runnable() {
                    @Override
                    public void run() {
                        setIsConnectText("连接中...");
                        if (mService != null) {
                            Logger.d("mService.connect354");
                            mService.connect(currentMacAddress);
                        }
                    }
                }, 200);

            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy");
        if (this.isBind) {
            unbindService(mServiceConnection);
            this.isBind = false;
        }
        this.removeConnectBluetoothTimeOut();
        this.removeReConnectBlueTooth();
        Logger.d("onDestroy mWebView != null =" + (mWebView != null));
        if (mWebView != null) {
            mWebView.onPause();
            mWebView.clearCache(true);
            mWebView.clearFormData();
            mWebView.clearHistory();
            mWebView.destroy();
        }
        Logger.d("onDestroy timer != null =" + (timer != null));
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception e) {
            Logger.d("unregisterReceiver", e);
        }
        Logger.d("onDestroy mService != null =" + (mService != null));
        if (mService != null) {
            mService.destroy();
            mService.stopSelf();
            mService = null;
            Logger.d("service 置空");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "蓝牙打开成功", Toast.LENGTH_SHORT).show();
                    Logger.d("onActivityResult");
                    service_init();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Logger.d("BT not enabled");
                    Toast.makeText(this, "不开启蓝牙将无法同步数据", Toast.LENGTH_SHORT).show();
                }
                break;
            case ACTIVITY_FOR_RESULT_REQUEST_CODE_SELECT_DEVICE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    getDeviceList();
                    loadUrl(getUrl(SPUitl.getToken()));
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_brush_teeth_record:
            case R.id.nav_member_point:
            case R.id.nav_panda_store:
            case R.id.nav_wx_friend:
            case R.id.nav_typeface:
                Toasts.showShort("功能暂未开放");
                break;
            case R.id.nav_logout:
                SPUitl.clear();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @OnClick({R.id.ll_member_point, R.id.ll_panda_store, R.id.ll_typeface, R.id.ll_wx_friend, R.id.btn, R.id.btn_dismiss, R.id.ll_helper, R.id.ll_tb_setting, R.id.ll_about, R.id.ll_auto_test})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_member_point:
            case R.id.ll_panda_store:
                break;
            case R.id.ll_wx_friend://绑定公众号
                JumpToBizProfile.Req req = new JumpToBizProfile.Req();
                req.toUserName = "gh_3afe6eac55e3"; //公众号原始ID
                req.profileType = JumpToBizProfile.JUMP_TO_NORMAL_BIZ_PROFILE;
                req.extMsg = "";
                api.sendReq(req);
                Toasts.showShort("功能暂未开放");
                break;
            case R.id.ll_typeface:
                startActivity(new Intent(this, LanguageSwitchActivity.class));
                break;
            case R.id.btn:
                SPUitl.clear();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_dismiss:
                if (rlGuide != null) {
                    rlGuide.setVisibility(View.GONE);
                }
                if (rlContent != null) {
                    rlContent.setVisibility(View.VISIBLE);
                }
                SPUitl.saveFirsRun(false);
                break;
            case R.id.ll_helper:
                HelperActivity.start(this, this.data != null && !this.data.isEmpty(), this.isBltConnect);
                break;
            case R.id.ll_tb_setting:
                ToothbrushSettingActivity.start(this, this.isBltConnect);
                break;
            case R.id.ll_about:
                AboutActivity.start(this);
                break;
            case R.id.ll_auto_test:
                AutoTestActivity.start(this, this.data != null && !this.data.isEmpty(), this.isBltConnect);
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    /**
     * 右上角选择菜单
     */
    @SuppressLint("RtlHardcoded")
    public void popUpMyOverflow() {
        //获取状态栏高度
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        //状态栏高度+toolbar的高度
        int yOffset = frame.top + appbar.getHeight();
        if (null == mPopupWindow) {
            //初始化PopupWindow的布局
            View popView = getLayoutInflater().inflate(R.layout.action_overflow_popwindow, llSwitchDevice, false);
            //popView即popupWindow的布局，true设置focusAble.
            mPopupWindow = new PopupWindow(popView, (int) DensityUtil.dp(130), ViewGroup.LayoutParams.WRAP_CONTENT, true);
            //必须设置BackgroundDrawable后setOutsideTouchable(true)才会有效
            mPopupWindow.setBackgroundDrawable(new ColorDrawable());
            //点击外部关闭。
            mPopupWindow.setOutsideTouchable(true);
            //设置一个动画。
            mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
            //设置Gravity，让它显示在右上角。
            mPopupWindow.showAtLocation(appbar, Gravity.RIGHT | Gravity.TOP, 0, yOffset);
            //设置item的点击监听
            popView.findViewById(R.id.ll_item1).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentHelper.gotoAddDeviceActivity(UrlDetailActivity.this);
                    mPopupWindow.dismiss();
                }
            });
            popView.findViewById(R.id.ll_item2).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(UrlDetailActivity.this, AddBlueToothDeviceActivity.class);
                    startActivityForResult(intent, ACTIVITY_FOR_RESULT_REQUEST_CODE_SELECT_DEVICE);
                    mPopupWindow.dismiss();
                }
            });
        } else {
            mPopupWindow.showAtLocation(appbar, Gravity.RIGHT | Gravity.TOP, 0, yOffset);
        }

    }

    /**
     * 蓝牙设备选择菜单
     */
    public void initPopDeviceList(final List<DeviceListEntity.DevicesBean> data, boolean isShowView) {
        //获取状态栏高度
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        //状态栏高度+toolbar的高度
        int yOffset = frame.top + appbar.getHeight();
        if (null == mDevicePop) {
            //初始化PopupWindow的布局
            View popView = getLayoutInflater().inflate(R.layout.pop_device_list, llSwitchDevice, false);
            //popView即popupWindow的布局，true设置focusAble.
            WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
            int popHeight = wm.getDefaultDisplay().getHeight() - yOffset;
            mDevicePop = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, popHeight, true);
            //必须设置BackgroundDrawable后setOutsideTouchable(true)才会有效
            mDevicePop.setBackgroundDrawable(new ColorDrawable());
            //点击外部关闭。
            mDevicePop.setOutsideTouchable(true);
            //设置一个动画。
            mDevicePop.setAnimationStyle(android.R.style.Animation_Dialog);
            //设置Gravity，让它显示在右上角。
            if (isShowView) {
                mDevicePop.showAtLocation(appbar, Gravity.TOP, 0, yOffset);
            }
            //设置点击事件
            RecyclerView rv = popView.findViewById(R.id.rv);
            LinearLayout llOutView = popView.findViewById(R.id.ll_out_view);
            llOutView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDevicePop.dismiss();
                }
            });
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.VERTICAL, 1));
            PopDeviceAdapter popDeviceAdapter = new PopDeviceAdapter(data);
            popDeviceAdapter.setOnItemClickListener(new OnItemClickListener<DeviceListEntity.DevicesBean>() {
                @Override
                public void onItemClick(View v, DeviceListEntity.DevicesBean bean, int position) {
                    if (bean == null || (!TextUtils.isEmpty(currentMacAddress) && currentMacAddress.replaceAll(":", "").equals(bean.getDeviceid()))) {
                        mDevicePop.dismiss();
                        return;
                    }
                    if (mService != null) {
                        setTvDeviceNameText(String.format(Locale.getDefault(), "%1$s-%2$s", bean.getUsername(), bean.getDeviceid()));
                        StringBuilder sb = new StringBuilder(bean.getDeviceid());
                        for (int i = 0; i < sb.length(); i++) {
                            if (i % 3 == 0) {
                                sb.insert(i, ":");
                            }
                        }
                        sb.delete(0, 1);
                        currentMacAddress = sb.toString();
                        if (mDevice != null) {
                            mService.disconnect();
                        }
                        if (data.size() > 0) {
                            mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(currentMacAddress);
                            postDelayedOnUIThread(appbar, new Runnable() {
                                @Override
                                public void run() {
                                    setIsConnectText("连接中...");
                                    if (mService != null) {
                                        Logger.d("mService.connect608");
                                        mService.connect(currentMacAddress);
                                    }
                                }
                            }, 200);

                        }
                        mDevicePop.dismiss();
                    } else {
                        Toasts.showShort("蓝牙设备没有初始化");
                    }

                }
            });
            rv.setAdapter(popDeviceAdapter);
        } else {
            if (isShowView) {
                mDevicePop.showAtLocation(appbar, Gravity.TOP, 0, yOffset);
            }
        }

    }

    private void getDeviceList() {
        APIService api = new APIFactory().create(APIService.class);
        Subscription s = api.getDeviceList(SPUitl.getToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DeviceListEntity>() {
                    @Override
                    public void call(DeviceListEntity deviceListEntity) {
                        if (Constants.SUCCESS == deviceListEntity.getCode()) {
                            data.clear();
                            if (deviceListEntity.getDevices().size() != 0) {
                                for (DeviceListEntity.DevicesBean db : deviceListEntity.getDevices()) {
                                    if (!db.getDeviceid().contains(":")) {
                                        data.add(db);
                                    }
                                }
                                if (data.size() != 0) {
                                    setSwitchDeviceVisibility(true);
                                    setToolBarTitleVisibility(false);
                                    setTvDeviceNameText(String.format(Locale.getDefault(), "%1$s-%2$s", data.get(0).getUsername(), data.get(0).getDeviceid()));
                                    //发现绑定设备  连接并尝试同步数据  第一次连接
                                    StringBuilder sb = new StringBuilder(data.get(0).getDeviceid());
                                    for (int i = 0; i < sb.length(); i++) {
                                        if (i % 3 == 0) {
                                            sb.insert(i, ":");
                                        }
                                    }
                                    sb.delete(0, 1);
                                    currentMacAddress = sb.toString();
                                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(currentMacAddress);
                                    if (mDevice != null && mService != null && !mService.hasConnected(currentMacAddress)) {
                                        Logger.d("getDeviceList disconnect");
                                        mService.disconnect();
                                    }
                                    postDelayedOnUIThread(appbar, new Runnable() {
                                        @Override
                                        public void run() {
                                            if (isBltConnect)
                                                return;
                                            sendConnectBluetoothTimeOut();
                                            setIsConnectText("连接中...");
                                            if (mService != null) {
                                                Logger.d("mService.connect672");
                                                mService.connect(currentMacAddress);
                                            }
                                        }
                                    }, 200);
                                }
                            } else {
                                setSwitchDeviceVisibility(false);
                                setToolBarTitleVisibility(true);
                            }

                        } else {
                            showMessage(deviceListEntity.getMessage());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showMessage("登录失败，请检查网络");
                        Logger.d("url.call", throwable);
                    }
                });
        addSubscription(s);
    }

    private boolean isBind = false;
    private BluetoothDevice mDevice = null;
    public static UartService mService = null;

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        isBind = bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            if (mService != null && mService.initialize()) {
                Logger.d("蓝牙连接service 初始化成功");
                getDeviceList();
            } else {
                Logger.e("Unable to initialize Bluetooth");
                finish();
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            Logger.d("classname--->" + classname.getClassName());
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        intentFilter.addAction(UartService.DEVICE_REFRESH_FALG);
        return intentFilter;
    }

    Timer timer = null;
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
                    runOnUiThread(new Runnable() {
                        public void run() {
                            isBltConnect = true;
                            postDelayedOnUIThread(appbar, new Runnable() {
                                @Override
                                public void run() {
                                    dismiss();
                                    setIsConnectText("正在同步数据中...");
                                    Logger.d("writeRXCharacteristic.writeRXCharacteristic");
                                    writeRXCharacteristic(bleProtoProcess.getRequests((byte) 1, (byte) 0));
                                    bleProtoProcess.setIsreqenddatas(false);
                                    bleProtoProcess.setHasrecieved(false);
                                }
                            }, 1000);
                        }
                    });
                    break;
                }
                case UartService.ACTION_GATT_DISCONNECTED: {
                    postDelayedOnUIThread(new Runnable() {
                        public void run() {
                            isBltConnect = false;
                            dismiss();
                            setIsConnectText("未连接");
//                            mService.close();
                        }
                    });
                    break;
                }
                case UartService.ACTION_GATT_SERVICES_DISCOVERED: {
                    if (mService != null)
                        mService.enableTXNotification();
                    break;
                }
                case UartService.ACTION_DATA_AVAILABLE: {
                    timecount = 0;
                    checkCount = 0;
                    final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                    int status = bleProtoProcess.interp(txValue);

                    switch (status) {
                        case BLEProtoProcess.BLE_DATA_START:
                        case BLEProtoProcess.BLE_RESULT_START:
                            transferTimeStart = System.currentTimeMillis();
                            Logger.d("BLE_DATA_START  and  BLE_RESULT_START");
                            setIsConnectText("正在同步数据中...");
                            bleProtoProcess.setHasrecieved(true);
                            runtype = 1;
                            timer = new Timer();
                            timer.schedule(new DataProcessTimer(), 0, 200);
                            break;
                        case BLEProtoProcess.BLE_DATA_RECEIVER:
                            break;
                        case BLEProtoProcess.BLE_DATA_END:
                        case BLEProtoProcess.BLE_RESULT_END:
                            runtype = 2;
                            timecount = 100;
                            break;
                        case BLEProtoProcess.BLE_MISSED_RECEIVER:
                            break;
                        case BLEProtoProcess.BLE_MISSED_END:
                            RunTimeLog.getInstance(UrlDetailActivity.this).log(RunTimeLog.LogAction.TRANSMIT, RunTimeLog.Result.SUCCESS, "丢失贞接收完毕", Util.getUseTime(transferTimeStart));
                            Logger.d("丢失帧接受完毕");
                            timecount = 100;
                            break;
                        case BLEProtoProcess.BLE_NO_SYNC://没有同步数据
                            if (bleProtoProcess.isHasrecieved()) {
                                Logger.d("请求动画");
                                bleProtoProcess.setIsreqenddatas(true);
                                writeRXCharacteristic(bleProtoProcess.getRequests((byte) 0, (byte) 1));
                                RunTimeLog.getInstance(UrlDetailActivity.this).log(RunTimeLog.LogAction.TRANSMIT, RunTimeLog.Result.ANIM, "请求动画", 0);
                            } else {
                                Logger.d("没有数据同步");
                                setIsConnectText("已连接");
                                rlTips.setVisibility(View.VISIBLE);
                                Toasts.showShort("没有数据同步");
                                RunTimeLog.getInstance(UrlDetailActivity.this).log(RunTimeLog.LogAction.TRANSMIT, RunTimeLog.Result.END, "无数据可同步", 0);
                            }
                            break;
                    }
                    break;
                }
                case UartService.DEVICE_DOES_NOT_SUPPORT_UART: {
                    Logger.d("Device doesn't support UART. Disconnecting");
                    if (mService != null)
                        mService.disconnect();
                    break;
                }
                case UartService.DEVICE_REFRESH_FALG: {
                    Logger.d("refresh");
                    Toasts.showShort("refresh");
                    break;
                }
            }
        }
    };

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case WHAT_CONNECT_BLUETOOTH_TIMEOUT:
                this.removeConnectBluetoothTimeOut();
                if (this.isBltConnect)
                    return true;
                dismiss();
                setIsConnectText("未连接");
                this.sendReConnectBlueTooth();
                break;
            case WHAT_RECONNECT_BLUETOOTH:
                this.removeReConnectBlueTooth();
                if (this.isBltConnect)
                    return true;
                // 打开蓝牙
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else if (mService == null) {
                    this.service_init();
                } else {
                    this.getDeviceList();
                }
                break;
            default:
                return false;
        }
        return true;
    }

    private void sendConnectBluetoothTimeOut() {
        this.removeConnectBluetoothTimeOut();
        if (this.handler == null || this.isBltConnect)
            return;
        this.handler.sendEmptyMessageDelayed(WHAT_CONNECT_BLUETOOTH_TIMEOUT, DELAYMILLIS_CONNECT_BLUETOOTH);
    }

    private void removeConnectBluetoothTimeOut() {
        if (this.handler == null)
            return;
        this.handler.removeMessages(WHAT_CONNECT_BLUETOOTH_TIMEOUT);
    }

    private void sendReConnectBlueTooth() {
        this.removeReConnectBlueTooth();
        if (this.isBltConnect || this.handler == null)
            return;
        handler.sendEmptyMessageDelayed(WHAT_RECONNECT_BLUETOOTH, DELAYMILLIS_RECONNECT_BLUETOOTH);
    }

    private void removeReConnectBlueTooth() {
        if (this.handler == null)
            return;
        this.handler.removeMessages(WHAT_RECONNECT_BLUETOOTH);
    }

    private class DataProcessTimer extends TimerTask {  //1s

        @Override
        public void run() {
            Logger.d(String.format(Locale.getDefault(), "计时器开始执行count:%1$d;runtype:%2$d", timecount, runtype));
            if (runtype == 0) {//非接收数据过程，什么也不执行，//可以释放timer
                timecount = 0;
                if (timer != null) {
                    timer.cancel();
                }
            } else {
                //1 接收数据    2-核对数据
                if ((runtype == 1 && timecount >= 10) || (runtype == 2 && timecount >= 4)) {
                    timecount = 0;
                    if (checkData()) {
                        runtype = 0;
                        if (timer != null) {
                            timer.cancel();
                        }
                    }
                }
                timecount++;
            }
        }
    }

    private boolean checkData() {
        checkCount++;
        try {
            if (bleProtoProcess.checkMissed() && this.checkCount <= 5) {
                Logger.d("丢帧");
                RunTimeLog.getInstance(this).log(RunTimeLog.LogAction.TRANSMIT, RunTimeLog.Result.ERROR, "丢帧，重新获取", Util.getUseTime(transferTimeStart));
                this.writeRXCharacteristic(bleProtoProcess.getMissedRequests());
                return false;
            } else {
                //1.发送请求成功帧  2.把数据交给后台处理
                Logger.d("数据接收完毕!");
                this.writeRXCharacteristic(bleProtoProcess.getCompleted());
                //------------发送数据到服务器
                if (bleProtoProcess.isreqenddatas()) {
                    RunTimeLog.getInstance(this).log(RunTimeLog.LogAction.TRANSMIT, RunTimeLog.Result.SUCCESS, "接收数据成功，准备发往服务器", Util.getUseTime(transferTimeStart));
                    uploadData();
                }
                return true;
            }
        } catch (Exception e) {
            Logger.e("checkData", e);
        }
        return true;
    }

    private void writeRXCharacteristic(byte[] value) {
        if (mService != null) {
            mService.writeRXCharacteristic(value);
        }
    }

    private void uploadData() {
        this.updateTimeStart = System.currentTimeMillis();
        bleProtoProcess.setHasrecieved(false);
        bleProtoProcess.setIsreqenddatas(false);
        APIService api = new APIFactory().create(APIService.class);
        String address = currentMacAddress.replaceAll(":", "");
        String str = "设备地址：" + address + "-" + "Software：" + bleProtoProcess.getSoftware() + "-" + "Factory：" + bleProtoProcess.getFactory() + "-" + "Model：" + bleProtoProcess.getModel() + "-" + "Power：" + bleProtoProcess.getPower() + "-" + "Time：" + bleProtoProcess.getTime() + "-" + "Hardware：" + bleProtoProcess.getHardware() + "-" + bleProtoProcess.getDatatype() + "-";
        Logger.d(String.format(Locale.getDefault(), "uploadData:\nstr:%1$s\ntime:%2$s", str, String.valueOf(System.currentTimeMillis())));
        addSubscription(api.uploadData(address,
                String.valueOf(bleProtoProcess.getSoftware()),
                String.valueOf(bleProtoProcess.getFactory()),
                String.valueOf(bleProtoProcess.getModel()),
                String.valueOf(bleProtoProcess.getPower()),
                String.valueOf(bleProtoProcess.getTime()),
                String.valueOf(bleProtoProcess.getHardware()),
                bleProtoProcess.getBuffer(),
                String.valueOf(bleProtoProcess.getDatatype()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<WXEntity>() {
                    @Override
                    public void call(WXEntity wxEntity) {
                        String msg;
                        if (Constants.SUCCESS == wxEntity.getCode()) {
                            setIsConnectText("已连接");
                            rlTips.setVisibility(View.VISIBLE);
                            msg = "总共上传数据" + bleProtoProcess.getPagesSize() + "条，成功" + bleProtoProcess.getPagesSize() + "条，失败0条";
                            tvUpdateRecord.setText(msg);
                            bleProtoProcess.setPageSize(0);
                            loadUrl(getUrl(SPUitl.getToken()));
                        } else if (99 == wxEntity.getCode()) {
                            rlTips.setVisibility(View.VISIBLE);
                            msg = "总共上传数据" + 0 + "条，成功" + 0 + "条，失败" + bleProtoProcess.getPagesSize() + "条";
                            tvUpdateRecord.setText(msg);
                            Toasts.showShort("系统错误");
                        } else if (20002 == wxEntity.getCode()) {
                            rlTips.setVisibility(View.VISIBLE);
                            msg = "总共上传数据" + 0 + "条，成功" + 0 + "条，失败" + bleProtoProcess.getPagesSize() + "条";
                            tvUpdateRecord.setText(msg);
                            Toasts.showShort("设备版本未找到");
                        } else {
                            Toasts.showShort("未知错误");
                            rlTips.setVisibility(View.VISIBLE);
                            msg = "总共上传数据" + 0 + "条，成功" + 0 + "条，失败" + bleProtoProcess.getPagesSize() + "条";
                            tvUpdateRecord.setText(msg);
                            Logger.d("错误code ：" + wxEntity.getCode() + "错误信息：" + wxEntity.getMessage());
                        }
                        RunTimeLog.getInstance(UrlDetailActivity.this).log(RunTimeLog.LogAction.UPDATE, RunTimeLog.Result.SUCCESS, msg, Util.getUseTime(updateTimeStart));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        String msg = "总共上传数据" + 0 + "条，成功" + 0 + "条，失败" + bleProtoProcess.getPagesSize() + "条";
                        RunTimeLog.getInstance(UrlDetailActivity.this).log(RunTimeLog.LogAction.UPDATE, RunTimeLog.Result.FAILED, msg, Util.getUseTime(updateTimeStart));
                        Toasts.showShort("数据上传失败，请检查网络！");
                        rlTips.setVisibility(View.VISIBLE);
                        tvUpdateRecord.setText(msg);
                    }
                }));
    }

    private void setTvDeviceNameText(String text) {
        if (mTvDeviceName != null) {
            mTvDeviceName.setText(text);
        }
    }

    private void setIsConnectText(String text) {
        if (this.tvIsConnect != null) {
            this.tvIsConnect.setText(text);
        }
    }
//
//    private void setIsConnectText(@StringRes int resId) {
//        if (this.tvIsConnect != null) {
//            this.tvIsConnect.setText(resId);
//        }
//    }

    private void setToolBarTitleVisibility(boolean show) {
        if (this.mToolBarTitle != null) {
            this.mToolBarTitle.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void setSwitchDeviceVisibility(boolean show) {
        if (this.llSwitchDevice != null) {
            this.llSwitchDevice.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private String getUrl(String token) {
        boolean chinese = TextUtils.equals(getResources().getConfiguration().locale.getLanguage(), "zh");
        final String URL = "http://www.easylinkage.cn/webapp2/%1$sanalysis/today?id=361&index=0&r=0.4784193145863376&token=%2$s";
        return String.format(Locale.getDefault(), URL, chinese ? "" : "en/", token);
    }

    private DeviceListEntity.DevicesBean findDevice(int userId) {
        if (Util.isEmpty(this.data)) {
            return null;
        }
        for (DeviceListEntity.DevicesBean device : this.data) {
            if (device != null && device.getUserid() == userId) {
                return device;
            }
        }
        return null;
    }
}