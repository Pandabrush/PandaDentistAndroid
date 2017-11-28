package com.pandadentist.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by zhangwy on 2017/11/6 下午8:43.
 * Updated by zhangwy on 2017/11/6 下午8:43.
 * Description 蓝牙广播接收器
 */

public class BlueToothBroadcastReceiver extends BroadcastReceiver {

    private Callback callback;
    private boolean hasRegistered = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null)
            return;
        if (callback == null) {
            this.unRegister(context);
            return;
        }

        switch (intent.getAction()) {
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                callback.onDiscoveryStart();
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                callback.onDiscoveryFinished();
                break;
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device == null)
                    return;
                callback.onDeviceFound(device);
                break;
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void register(Context context, Callback callback) {
        if (context != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            this.setCallback(callback);
            context.registerReceiver(this, filter);
            this.hasRegistered = true;
        }
    }

    public void unRegister(Context context) {
        if (this.hasRegistered && context != null) {
            context.unregisterReceiver(this);
            this.setCallback(null);
            this.hasRegistered = false;
        }
    }

    public static interface Callback {

        void onDiscoveryStart();

        void onDiscoveryFinished();

        void onDeviceFound(BluetoothDevice device);
    }
}
