package com.pandadentist.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pandadentist.R;
import com.pandadentist.bleconnection.BLEManager;
import com.pandadentist.bleconnection.entity.ToothbrushEntity;
import com.pandadentist.bleconnection.entity.ToothbrushInfoEntity;
import com.pandadentist.bleconnection.scan.ScanBluetooth;
import com.pandadentist.bleconnection.utils.BLEProtoProcess;
import com.pandadentist.bleconnection.utils.Logger;
import com.pandadentist.bleconnection.utils.Toasts;
import com.pandadentist.bleconnection.utils.Util;
import com.pandadentist.entity.DeviceListEntity;
import com.pandadentist.ui.adapter.BlueToothDeviceAdapter;
import com.pandadentist.widget.ColorProgressBar;
import com.pandadentist.widget.RecycleViewDivider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;

/**
 * Updated by zhangwy on 2017/11/12
 */

@SuppressWarnings("IntegerDivisionInFloatingPointContext")
public class AddBlueToothDeviceActivity extends SwipeRefreshBaseActivity implements BLEManager.OnScanListener, BLEManager.OnConnectListener, BLEManager.OnToothbrushDataListener {

    private static final String EXTRA_HAS_DEVICE = "extraHasDevice";

    public static void start(Context context, boolean hasDevice) {
        Intent intent = new Intent(context, AddBlueToothDeviceActivity.class);
        intent.putExtra(EXTRA_HAS_DEVICE, hasDevice);
        context.startActivity(intent);
    }

    private static final int REQUEST_SELECT_DEVICE = 1;

    @Bind(R.id.ll_not_found)
    LinearLayout llNotFound;
    @Bind(R.id.ll_loading)
    LinearLayout llLoading;
    @Bind(R.id.ll_loading_tip)
    LinearLayout llLoadingTip;
    @Bind(R.id.rv)
    RecyclerView rv;
    @Bind(R.id.iv_loading)
    ImageView ivLoading;
    @Bind(R.id.iv_upload_loading)
    ImageView ivUploadLoading;
    @Bind(R.id.circle_progress_bar1)
    ColorProgressBar colorProgressBar;
    @Bind(R.id.tv_percent)
    TextView tvPercent;
    @Bind(R.id.tv_upload_tip)
    TextView tvUploadTip;
    @Bind(R.id.ll_upload)
    LinearLayout llUpload;
    @Bind(R.id.tv_page_size)
    TextView tvPageSize;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private BLEManager bleManager;
    private BluetoothDevice mDevice = null;
    private BlueToothDeviceAdapter mAdapter;
    private HashMap<String, BluetoothDevice> deviceHashMap = new HashMap<>();
    private Animation circle_anim;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (UrlDetailActivity.mService != null && !UrlDetailActivity.mService.initialize()) {
            Logger.d("不用初始化 service  直接读写数据");
            Toasts.showShort("Service 初始化失败");
            finish();
        }
        if (this.hasTopBar()) {
            this.topBar.setLeftVisibility(true);
            this.setOnLeftClickListener();
            this.topBar.setCentreText(R.string.connect_bluetooth);
            this.topBar.setRightText(R.string.stop_scan, false);
            this.topBar.setRightTextColor(Color.parseColor("#20CBE7"));
            this.topBar.setOnRightClickListener(() -> bleManager.stopScan());
        }

        new BLEProtoProcess().setOnZhenListener((zhen, total) -> {
            float percent = zhen / total * 100f;
            int ip = (int) percent;
            tvPercent.setText(String.format(Locale.getDefault(), "%1$d%%", ip));
            colorProgressBar.setValue(ip);
        });
        //开启动画
        circle_anim = AnimationUtils.loadAnimation(this, R.anim.blue_tooth_round_rotate);
        LinearInterpolator interpolator = new LinearInterpolator();  //设置匀速旋转，在xml文件中设置会出现卡顿
        circle_anim.setInterpolator(interpolator);
        ivLoading.startAnimation(circle_anim);
        //检查权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            int permission = this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        findViewById(R.id.btn).setOnClickListener(v -> scanBlueDevice());

        this.initView();
//        this.getDeviceList();

        this.bleManager = BLEManager.getInstance();
        this.bleManager.initialize(this);
        this.scanBlueDevice();
        this.bleManager.setConnectListener(this);
        this.bleManager.setToothbrushListener(this);
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_add_blue_tooth_device;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initView() {
        // 扫描显示列表
        // 初始化列表
        mAdapter = new BlueToothDeviceAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.VERTICAL, 1));
        rv.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((v, device, position, code) -> {
            String deviceAddress = device.getAddress();
            // 先绑定，在连接蓝牙
            // 绑定蓝牙设备
            switch (code) {
                case 1:
                    bleManager.reqData(deviceAddress);
                    break;
                case 2:
                    bleManager.disConnect(deviceAddress);
                    break;
                case 3:
                    bindDevice(deviceAddress);
                    break;
                case 4:
                    ToothbrushSettingActivity.start(AddBlueToothDeviceActivity.this, deviceAddress, true);
                    break;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                    ((TextView) findViewById(R.id.deviceName)).setText(String.format(Locale.getDefault(), "%s - connecting", mDevice.getName()));
                    UrlDetailActivity.mService.connect(deviceAddress);
                }
                break;
            case ScanBluetooth.REQUESTCODE_FROM_BLUETOOTH_ENABLE:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "蓝牙打开成功", Toast.LENGTH_SHORT).show();
                    this.scanBlueDevice();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Logger.d("BT not enabled");
                    Toast.makeText(this, "蓝牙不可用", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Logger.e("wrong request code");
                break;
        }
    }

    private void showNotFound() {
        if (rv != null) {
            rv.setEnabled(true);
            ivLoading.clearAnimation();
            llLoading.setVisibility(View.VISIBLE);
            llLoadingTip.setVisibility(View.GONE);
            llNotFound.setVisibility(View.VISIBLE);
        }
    }

    private void showList() {
        if (rv != null) {
            rv.setEnabled(true);
            ivLoading.clearAnimation();
            llLoading.setVisibility(View.VISIBLE);
            llLoadingTip.setVisibility(View.VISIBLE);
            llNotFound.setVisibility(View.GONE);
        }
    }

    private void scanBlueDevice() {
        if (this.bleManager != null) {
            this.bleManager.scan(this);
        }
    }

    private void stopLeScan() {
        if (this.bleManager != null) {
            this.bleManager.stopScan();
        }
    }

    @Override
    public void onScanStart() {
        this.showLoadingView();
    }

    @Override
    public void onScanDevice(BluetoothDevice device) {
        if (device == null)
            return;
        Logger.d("onScanDevice:" + device.getName());
        runOnUiThread(() -> postDelayedOnUIThread(() -> {
            String address = device.getAddress();
            if (deviceHashMap.containsKey(address))
                return;
            deviceHashMap.put(address, device);
            mAdapter.put(device);
            if (hasDevice()) {
                showList();
            }
        }));
    }

    @Override
    public void onScanEnd() {
        try {
            if (this.hasDevice()) {
                showList();
            } else {
                showNotFound();
            }
        } catch (Exception e) {
            Logger.e("scanDeviceFinished", e);
        }
    }

    @Override
    public void onScanError(int code) {
        String message = String.format(Locale.getDefault(), "error code = %d", code);
        Logger.d(message);
        Toasts.showLong(message);
    }

    private void showLoadingView() {
        if (rv != null) {
            rv.setEnabled(true);
            ivLoading.startAnimation(circle_anim);
            llLoading.setVisibility(View.VISIBLE);
            llLoadingTip.setVisibility(View.VISIBLE);
            llNotFound.setVisibility(View.GONE);
        }
    }

    private boolean hasDevice() {
        return !Util.isEmpty(this.deviceHashMap);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.stopLeScan();
    }

    private void bindDevice(final String mac) {
        this.bleManager.connect(mac);
    }

    @Override
    public void onConnected(String deviceId) {
        this.mAdapter.connected(deviceId, true);
    }

    @Override
    public void onDisConnected(String deviceId) {
        this.mAdapter.connected(deviceId, false);
    }

    @Override
    public void onConnectError(String deviceId, int errorCode) {
        Logger.d(String.format(Locale.getDefault(), "deviceId:%s;errorCode:%d", deviceId, errorCode));
    }

    @Override
    public void onReadStart(String deviceId) {
        Logger.d(String.format("read data start for device %s", deviceId));
    }

    @Override
    public void onData(String deviceId, ToothbrushInfoEntity infoEntity, ToothbrushEntity dataEntity) {
        Logger.d(String.format("read data end for device %s, and data = %s", deviceId, dataEntity.getContent()));
    }

    @Override
    public void onNoData(String deviceId) {
        Logger.d(String.format("no data for device %s", deviceId));
    }
}
