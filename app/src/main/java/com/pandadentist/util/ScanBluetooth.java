package com.pandadentist.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.pandadentist.R;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by zhangwy on 2018/1/10 下午12:58.
 * Updated by zhangwy on 2018/1/10 下午12:58.
 * Description 扫描蓝牙
 */
@SuppressWarnings("unused")
public abstract class ScanBluetooth {

    public static final int REQUESTCODE_FROM_BLUETOOTH_ENABLE = Integer.MAX_VALUE;

    public static ScanBluetooth create() {
        return new ScanBluetoothImpl();
    }

    public abstract boolean support(Activity activity);

//    public abstract boolean support(Fragment fragment);
    /**
     * 是否能扫描，调用该接口的类需要重写onActivityResult 接收requestCode为REQUESTCODE_FROM_BLUETOOTH_ENABLE参数
     *
     * @param activity 手机不支持蓝牙时关闭界面或者当蓝牙未打开需要去设置界面打开蓝牙
     * @return true 可以执行搜索 反之不能执行搜索
     */
    public abstract boolean canScan(Activity activity);

//    public abstract boolean canScan(Fragment fragment);

    public abstract boolean startLeScan(Activity activity, OnLeScanListener listener);

//    public abstract boolean startLeScan(Fragment fragment, OnLeScanListener listener);

    public abstract void stopLeScan();

    private static class ScanBluetoothImpl extends ScanBluetooth implements BluetoothAdapter.LeScanCallback, Handler.Callback {
        private final long DELAYED_SCAN = 10000; //蓝牙扫描时长10秒

        private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        private Handler handler = new Handler(Looper.getMainLooper(), this);
        private final int WHAT_ADD_DEVICE = 100;
        private final int WHAT_SCAN_TIMEOUT = 101;
        private OnLeScanListener listener;
        private HashMap<String, BluetoothDevice> deviceHashMap = new HashMap<>();

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
//
//        @Override
//        public boolean support(Fragment fragment) {
//            return this.support(fragment == null ? null : fragment.getActivity());
//        }

        @Override
        public boolean canScan(Activity activity) {
            if (!this.support(activity)) {
                return false;
            }
            if (!this.adapter.isEnabled()) {
                Toasts.showLong(R.string.no_open_bluetooth);
                if (activity != null) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    activity.startActivityForResult(enableIntent, REQUESTCODE_FROM_BLUETOOTH_ENABLE);
                }
                return false;
            }
            return true;
        }
//
//        @Override
//        public boolean canScan(Fragment fragment) {
//            if (!this.support(fragment == null ? null : fragment.getActivity())) {
//                return false;
//            }
//            if (!this.adapter.isEnabled()) {
//                Toasts.showLong(R.string.no_open_bluetooth);
//                if (fragment != null) {
//                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                    fragment.startActivityForResult(enableIntent, REQUESTCODE_FROM_BLUETOOTH_ENABLE);
//                }
//                return false;
//            }
//            return true;
//        }

        @Override
        public boolean startLeScan(Activity activity, OnLeScanListener listener) {
            this.setListener(listener);
            return this.canScan(activity) && this.startLeScan();
        }
//
//        @Override
//        public boolean startLeScan(Fragment fragment, OnLeScanListener listener) {
//            this.setListener(listener);
//            return this.canScan(fragment) && this.startLeScan();
//        }

        private void setListener(OnLeScanListener listener) {
            this.listener = listener;
        }

        private boolean startLeScan() {
            this.deviceHashMap.clear();
            this.adapter.startLeScan(this);
            this.handler.sendEmptyMessageDelayed(WHAT_SCAN_TIMEOUT, DELAYED_SCAN);
            if (this.listener != null) {
                this.listener.onLeScanStart();
            }
            return true;
        }

        @Override
        public void stopLeScan() {
            this.finalStopLeScan(false);
        }

        private void finalStopLeScan(boolean auto) {
            if (this.adapter != null) {
                this.adapter.stopLeScan(this);
            }
            if (this.handler.hasMessages(WHAT_SCAN_TIMEOUT)) {
                this.handler.removeMessages(WHAT_SCAN_TIMEOUT);
            }
            if (this.listener != null) {
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
                    this.finalStopLeScan(true);
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
            if (this.listener != null) {
                this.listener.onDevice(device);
            }
        }

    }

    public interface OnLeScanListener {

        void onLeScanStart();

        void onDevice(BluetoothDevice device);

        void onLeScanStop(boolean auto);
    }
}
