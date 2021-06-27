package com.pandadentist.bleconnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;

import com.pandadentist.bleconnection.entity.ToothbrushEntity;
import com.pandadentist.bleconnection.entity.ToothbrushInfoEntity;
import com.pandadentist.bleconnection.parse.Send2Buffer;
import com.pandadentist.bleconnection.parse.Transfer;
import com.pandadentist.bleconnection.service.BLEService;
import com.pandadentist.bleconnection.utils.Logger;

import java.util.HashMap;

/**
 * CreateTime 2021/6/24 23:09
 * Author zhangwy
 * desc:
 * <p>
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
@SuppressWarnings({"SameParameterValue", "unused"})
public class BLEBroadcastReceiver extends BroadcastReceiver implements Transfer.OnTransferCallback {
    private boolean destroy = false;
    private OnReceiverCallback callback;
    private Handler handler = new Handler(Looper.getMainLooper());
    private HashMap<String, Transfer> processMap = new HashMap<>();

    public BLEBroadcastReceiver(OnReceiverCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (this.checkDestroy(context) || intent == null || this.callback == null) {
            return;
        }

        final String action = intent.getAction();
        final String address = intent.getStringExtra(BLEService.EXTRA_ADDRESS);
        if (action == null || action.length() == 0
                || address == null || address.length() == 0) {
            return;
        }

        switch (action) {
            case BLEService.DEVICE_DOES_NOT_SUPPORT_UART: {
                Logger.d("Device doesn't support UART. Disconnecting");
                callback.onBleNonSupport(address);
                break;
            }
            case BLEService.ACTION_GATT_CONNECTED: {
                Logger.d("writeRXCharacteristic.writeRXCharacteristic");
                this.postDelayedOnMainThread(() -> this.sync(address), 1000);
                break;
            }

            case BLEService.ACTION_GATT_DISCONNECTED: {
                postOnMainThread(() -> callback.onDisconnected(address));
                break;
            }

            case BLEService.ACTION_GATT_SERVICES_DISCOVERED: {
                callback.onServicesDiscovered(address);
                break;
            }

            case BLEService.ACTION_DATA_AVAILABLE: {
                final byte[] txValue = intent.getByteArrayExtra(BLEService.EXTRA_DATA);
                process(address).transfer(txValue);
                break;
            }
        }
    }

    private boolean checkDestroy(Context context) {
        if (!this.isDestroy()) {
            return false;
        }
        try {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        } catch (Exception e) {
            Logger.d("unregisterReceiver", e);
        }
        return true;
    }

    public void sync(String address) {
        this.write(address, Send2Buffer.dataReq());
    }

    public void destroy() {
        this.handler.removeCallbacksAndMessages(null);
        this.handler = null;
        this.destroy = true;
        this.callback = null;
    }

    private boolean isDestroy() {
        return this.destroy;
    }

    private boolean checkData(String address) {
        return true;
    }

    private Transfer process(String address) {
        Transfer transfer = this.processMap.get(address);
        if (transfer == null) {
            transfer = new Transfer(address, this);
            this.processMap.put(address, transfer);
        }
        return transfer;
    }

    private void write(String address, byte[] value) {
        if (this.callback == null) {
            return;
        }
        this.callback.onWrite(address, value);
    }

    private void postDelayedOnMainThread(Runnable action, long delayMillis) {
        if (this.handler != null && action != null) {
            this.handler.postDelayed(action, delayMillis);
        }
    }

    private void postOnMainThread(Runnable action) {
        if (this.handler != null && action != null) {
            this.handler.post(action);
        }
    }

    @Override
    public void onSend2BLE(String deviceId, byte[] bytes) {
        this.write(deviceId, bytes);
    }

    @Override
    public void onComplete(String deviceId, ToothbrushInfoEntity tbInfo, ToothbrushEntity tbData) {
        this.postOnMainThread(() -> {
            if (callback == null) {
                return;
            }
            callback.onRead(deviceId, tbInfo, tbData);
        });
    }

    public interface OnReceiverCallback {

        void onDisconnected(String address);

        void onServicesDiscovered(String address);

        void onBleNonSupport(String address);

        void onReadStart(String address);

        void onRead(String address, ToothbrushInfoEntity infoEntity, ToothbrushEntity dataEntity);

        void onWrite(String address, byte[] bytes);

        void onNoData(String address);
    }
}
