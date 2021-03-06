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
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pandadentist.R;
import com.pandadentist.config.Constants;
import com.pandadentist.entity.DeviceListEntity;
import com.pandadentist.entity.WXEntity;
import com.pandadentist.listener.OnItemClickListener;
import com.pandadentist.listener.OnZhenListener;
import com.pandadentist.network.APIFactory;
import com.pandadentist.network.APIService;
import com.pandadentist.ui.adapter.BlueToothDeviceAdapter;
import com.pandadentist.util.BLEProtoProcess;
import com.pandadentist.util.Logger;
import com.pandadentist.util.SPUitl;
import com.pandadentist.util.ScanBluetooth;
import com.pandadentist.util.Toasts;
import com.pandadentist.util.Util;
import com.pandadentist.widget.ColorProgressBar;
import com.pandadentist.widget.RecycleViewDivider;
import com.pandadentist.widget.TopBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Updated by zhangwy on 2017/11/12
 */

public class AddBlueToothDeviceActivity extends SwipeRefreshBaseActivity implements ScanBluetooth.OnLeScanListener {

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
    private ScanBluetooth scanBluetooth;
    private BluetoothDevice mDevice = null;
    private BlueToothDeviceAdapter mAdapter;
    private HashMap<String, BluetoothDevice> deviceHashMap = new HashMap<>();
    private String macAddress;
    private Animation circle_anim;
    private List<DeviceListEntity.DevicesBean> remoteDevices = new ArrayList<>();

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
            this.topBar.setRightText(R.string.help, false);
            this.topBar.setRightTextColor(Color.parseColor("#20CBE7"));
            this.topBar.setOnRightClickListener(new TopBar.OnClickListener() {
                @Override
                public void onClick() {
                    startActivity(new Intent(AddBlueToothDeviceActivity.this, BlueHelperActivity.class));
                }
            });
        }

        new BLEProtoProcess().setOnZhenListener(new OnZhenListener() {
            @Override
            public void onZhen(int zhen, int total) {
                float percent = zhen / total * 100f;
                int ip = (int) percent;
                tvPercent.setText(String.format(Locale.getDefault(), "%1$d%", ip));
                colorProgressBar.setValue(ip);
            }
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

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanBlueDevice();
            }
        });

        this.initView();
        this.getDeviceList();

        this.scanBluetooth = ScanBluetooth.create();
        if (this.scanBluetooth.canScan(this)) {
            this.scanBlueDevice();
        }
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
        mAdapter.setOnItemClickListener(new OnItemClickListener<BluetoothDevice>() {
            @Override
            public void onItemClick(View v, BluetoothDevice device, int position) {
                // 先绑定，在连接蓝牙
                // 绑定蓝牙设备
                if (remoteDevices.size() == 0) {
                    String deviceAddress = device.getAddress();
                    stopLeScan();
                    bindDevice(deviceAddress);
                } else {
                    Toasts.showShort("一个账户只能绑定一个设备，请先解除绑定！");
                }
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
        if (this.scanBluetooth != null) {
            this.scanBluetooth.startLeScan(this, this);
        }
    }

    private void stopLeScan() {
        if (this.scanBluetooth != null) {
            this.scanBluetooth.stopLeScan();
        }
    }

    @Override
    public void onLeScanStart() {
        this.showLoadingView();
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

    @Override
    public void onDevice(final BluetoothDevice device) {
        if (device == null)
            return;
        Logger.d("addDevices" + device.getName());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                postDelayedOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        String address = device.getAddress();
                        if (deviceHashMap.containsKey(address))
                            return;
                        deviceHashMap.put(address, device);
                        mAdapter.replace(deviceHashMap.values());
                        if (hasDevice()) {
                            showList();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onLeScanStop(boolean auto) {
        this.scanDeviceFinished();
    }

    private void scanDeviceFinished() {
        try {
            if (this.hasDevice()) {
                showList();
            } else {
                showNotFound();
            }
            this.mAdapter.replace(this.deviceHashMap.values());
        } catch (Exception e) {
            Logger.e("scanDeviceFinished", e);
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
        APIService api = new APIFactory().create(APIService.class);
        Subscription s = api.bindDevice(mac.replaceAll(":", ""), SPUitl.getToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<WXEntity>() {
                    @Override
                    public void call(WXEntity wxEntity) {
                        postDelayedOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac);
                                macAddress = mac;
                                Bundle b = new Bundle();
                                b.putString(BluetoothDevice.EXTRA_DEVICE, macAddress);

                                Intent result = new Intent();
                                result.putExtras(b);
                                setResult(Activity.RESULT_OK, result);
                                finish();
                                // 返回首页更新数据
                            }
                        });
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        dismiss();
                        Toasts.showShort("服务器连接失败！请检查手机网络！");
                        Log.d("throwable", "throwable-->" + throwable.toString());
                    }
                });
        addSubscription(s);
    }

    private void getDeviceList() {
        showProgress();
        APIService api = new APIFactory().create(APIService.class);
        Subscription s = api.getDeviceList(SPUitl.getToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DeviceListEntity>() {
                    @Override
                    public void call(DeviceListEntity deviceListEntity) {
                        dismiss();
                        if (Constants.SUCCESS == deviceListEntity.getCode()) {
                            for (DeviceListEntity.DevicesBean db : deviceListEntity.getDevices()) {
                                if (!db.getDeviceid().contains(":")) {
                                    remoteDevices.add(db);
                                }
                            }
                            Logger.d("size-->" + remoteDevices.size());
                        } else {
                            Toasts.showShort(deviceListEntity.getMessage());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Toasts.showShort("请检查网络!");
                        dismiss();
                        Logger.d("getDeviceList", throwable);
                    }
                });
        addSubscription(s);
    }
}
