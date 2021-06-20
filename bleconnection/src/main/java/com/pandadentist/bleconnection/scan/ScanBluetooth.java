package com.pandadentist.bleconnection.scan;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.pandadentist.bleconnection.R;
import com.pandadentist.bleconnection.utils.Logger;
import com.pandadentist.bleconnection.utils.Toasts;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by zhangwy on 2018/1/10 下午12:58.
 * Updated by zhangwy on 2018/1/10 下午12:58.
 * Description 扫描蓝牙
 */
@SuppressWarnings({"unused", "UnusedReturnValue", "FieldCanBeLocal"})
public abstract class ScanBluetooth {

    public static final int REQUESTCODE_FROM_BLUETOOTH_ENABLE = Integer.MAX_VALUE;

    public static ScanBluetooth create() {
        return new ScanBluetoothImpl();
    }

    public abstract boolean support(Activity activity);

    /**
     * 是否能扫描，调用该接口的类需要重写onActivityResult 接收requestCode为REQUESTCODE_FROM_BLUETOOTH_ENABLE参数
     *
     * @param activity 手机不支持蓝牙时关闭界面或者当蓝牙未打开需要去设置界面打开蓝牙
     * @return true 可以执行搜索 反之不能执行搜索
     */
    public abstract boolean canScan(Activity activity);

    /**
     * 是否能扫描，调用该接口的类需要重写onActivityResult
     *
     * @param activity    手机不支持蓝牙时关闭界面或者当蓝牙未打开需要去设置界面打开蓝牙
     * @param requestCode 启动蓝牙打开界面的requestCode
     * @return true 可以执行搜索 反之不能执行搜索
     */
    public abstract boolean canScan(Activity activity, int requestCode);

    public abstract boolean startLeScan(Activity activity, OnLeScanListener listener);

    public abstract void stopLeScan();

    private static class ScanBluetoothImpl extends ScanBluetooth implements BluetoothAdapter.LeScanCallback, Handler.Callback {
        private final long DELAYED_SCAN = 10000; //蓝牙扫描时长10秒

        private final int WHAT_ADD_DEVICE = 100;
        private final int WHAT_SCAN_TIMEOUT = 101;
        private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        private Handler handler = new Handler(Looper.getMainLooper(), this);
        private HashMap<String, BluetoothDevice> deviceHashMap = new HashMap<>();
        private OnLeScanListener listener;
        private boolean scaning = false;

        @Override
        public boolean support(Activity activity) {
            if (this.adapter == null) {
                this.adapter = BluetoothAdapter.getDefaultAdapter();
            }

            boolean support = this.adapter != null;
            if (!support) {
                Toasts.showLong(R.string.no_supported_bluetooth);
                if (activity != null) {
                    activity.finish();
                }
            }
            return support;
        }

        @Override
        public boolean canScan(Activity activity) {
            return this.canScan(activity, REQUESTCODE_FROM_BLUETOOTH_ENABLE);
        }

        @Override
        public boolean canScan(Activity activity, int requestCode) {
            if (!this.support(activity)) {
                return false;
            }
            if (this.adapter.isEnabled()) {
                return true;
            }
            Toasts.showLong(R.string.no_open_bluetooth);
            if (activity == null) {
                return false;
            }
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableIntent, requestCode);
            return false;
        }

        @Override
        public boolean startLeScan(Activity activity, OnLeScanListener listener) {
            this.setListener(listener);
            return this.canScan(activity) && this.startLeScan();
        }

        private void setListener(OnLeScanListener listener) {
            this.listener = listener;
        }

        private boolean startLeScan() {
            if (this.scaning) {
                this.finalStopLeScan(true, false);
                this.scaning = false;
            }
            this.deviceHashMap.clear();
            this.adapter.startLeScan(this);
            this.handler.sendEmptyMessageDelayed(WHAT_SCAN_TIMEOUT, DELAYED_SCAN);
            if (this.canCallback()) {
                this.listener.onLeScanStart();
            }
            this.scaning = true;
            return true;
        }

        @Override
        public void stopLeScan() {
            this.finalStopLeScan(false, true);
        }

        private void finalStopLeScan(boolean auto, boolean callback) {
            this.scaning = false;
            if (this.adapter != null) {
                this.adapter.stopLeScan(this);
            }
            if (this.handler.hasMessages(WHAT_SCAN_TIMEOUT)) {
                this.handler.removeMessages(WHAT_SCAN_TIMEOUT);
            }
            if (this.canCallback() && callback) {
                this.listener.onLeScanStop(auto);
            }
        }

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device == null)
                return;
            String name = device.getName();
            Logger.d(String.format(Locale.getDefault(), "addDevices:%1$s", name));
            if (TextUtils.isEmpty(name) || !name.contains("PBrush"))
                return;
            this.handler.sendMessage(this.handler.obtainMessage(WHAT_ADD_DEVICE, device));
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_SCAN_TIMEOUT:
                    this.finalStopLeScan(true, true);
                    break;
                case WHAT_ADD_DEVICE:
                    this.addDevice((BluetoothDevice) msg.obj);
                    break;
            }
            return false;
        }

        private void addDevice(BluetoothDevice device) {
            if (device == null)
                return;
            String address = device.getAddress();
            if (deviceHashMap.containsKey(address))
                return;
            deviceHashMap.put(address, device);
            if (this.canCallback()) {
                this.listener.onDevice(device);
            }
        }

        private boolean canCallback() {
            return this.listener != null;
        }
    }

    public interface OnLeScanListener {

        void onLeScanStart();

        void onDevice(BluetoothDevice device);

        void onLeScanStop(boolean auto);
    }
}
