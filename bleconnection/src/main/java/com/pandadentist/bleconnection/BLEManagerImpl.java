package com.pandadentist.bleconnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.pandadentist.bleconnection.entity.ToothbrushEntity;
import com.pandadentist.bleconnection.scan.ScanBluetooth;
import com.pandadentist.bleconnection.service.BLEService;
import com.pandadentist.bleconnection.utils.Logger;

import java.util.List;

/**
 * CreateTime 2021/6/20 14:27
 * Author zhangwy
 * desc:
 * <p>
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
public class BLEManagerImpl extends BLEManager implements ScanBluetooth.OnLeScanListener, ServiceConnection, BLEBroadcastReceiver.OnReceiverCallback {

    private Context bindContext;
    private ScanBluetooth scanBluetooth;
    private OnScanListener scanListener;
    private OnConnectListener connectListener;
    private OnToothbrushDataListener dataListener;
    private OnMotorListener motorListener;
    //    private List<String> connectDevices = new ArrayList<>();
    private BLEService bleService;
    private BLEBroadcastReceiver receiver = new BLEBroadcastReceiver(this);

    @Override
    public void initialize(Context context) {
        this.bindContext = context;
        Intent intent = new Intent(this.bindContext, BLEService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
        {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
            intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
            intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
            intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
            intentFilter.addAction(BLEService.DEVICE_DOES_NOT_SUPPORT_UART);
            LocalBroadcastManager.getInstance(this.bindContext).registerReceiver(this.receiver, intentFilter);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        Logger.d("onServiceConnected：" + name.getShortClassName());
        this.bleService = ((BLEService.LocalBinder) binder).getService();
        this.bleService.initialize();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Logger.d("onServiceDisconnected：" + name.getShortClassName());
        this.bleService = null;
    }

    @Override
    public void onDisconnected(String address) {
        if (this.connectListener == null) {
            return;
        }
        this.connectListener.onDisConnected(address);
    }

    @Override
    public void onServicesDiscovered(String address) {
        if (this.invalidService()) {
            return;
        }
        this.bleService.enableTXNotification(address);
    }

    @Override
    public void onBleNonSupport(String address) {
        if (this.connectListener == null) {
            return;
        }
        this.connectListener.onConnectError(address, Content.CODE_ERROR_BLE_NONSUPPORT);
    }

    @Override
    public void onReadStart(String address) {
        if (this.dataListener == null) {
            return;
        }
        this.dataListener.onReadStart(address);
    }

    @Override
    public void onRead(String address, ToothbrushEntity entity) {
        if (this.dataListener == null) {
            return;
        }
        this.dataListener.onData(address, entity);
    }

    @Override
    public void onWrite(String address, byte[] bytes) {
        if (this.invalidService()) {
            return;
        }
        this.bleService.writeRXCharacteristic(address, bytes);
    }

    @Override
    public void onNoData(String address) {
        if (this.dataListener == null) {
            return;
        }
        this.dataListener.onNoData(address);
    }

    private boolean invalidService() {
        return this.bleService == null;
    }

    @Override
    public void destroy() {
        this.stopScan();
        if (!this.invalidService()) {
            this.bleService.destroy();
            this.bleService.stopSelf();
            this.bindContext.unbindService(this);
        }

        try {
            LocalBroadcastManager.getInstance(this.bindContext).unregisterReceiver(this.receiver);
            this.receiver.destroy();
            this.receiver = null;
        } catch (Exception e) {
            Logger.e("unregisterReceiver", e);
        }
        this.scanListener = null;
        this.bindContext = null;
        this.setConnectListener(null);
        this.setToothbrushListener(null);
        this.setMotorListener(null);
    }

    @Override
    public boolean isEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    @Override
    public void scan(OnScanListener listener) {
        if (this.scanBluetooth != null) {
            if (this.scanBluetooth.isScanning()) {
                this.onScanError(Content.CODE_ERROR_BLE_SCANNING, false);
                return;
            }
            this.scanBluetooth.setListener(null);
        }
        this.scanListener = listener;
        this.scanBluetooth = ScanBluetooth.create();
        if (!this.scanBluetooth.support()) {
            this.onScanError(Content.CODE_ERROR_BLE_NONSUPPORT);
            return;
        }
        if (!this.scanBluetooth.canScan()) {
            this.onScanError(Content.CODE_ERROR_BLE_UNOPENED);
            return;
        }
        if (this.scanBluetooth.startLeScan(this)) {
            if (this.scanCanCallback()) {
                this.scanListener.onScanStart();
            }
        } else {
            this.onScanError(Content.CODE_ERROR_BLE_START_SCAN);
        }
    }

    @Override
    public void onLeDevice(BluetoothDevice device) {
        if (this.scanCanCallback()) {
            this.scanListener.onScanDevice(device);
        }
    }

    @Override
    public void stopScan() {
        if (this.scanBluetooth != null) {
            this.scanBluetooth.stopLeScan();
        }
        if (this.scanCanCallback()) {
            this.scanListener.onScanEnd();
        }
        this.scanListener = null;
    }

    @Override
    public boolean connect(String deviceId) {
        return this.connect(deviceId, true);
    }

    @Override
    public boolean connect(String deviceId, boolean increase) {
        if (!increase) {
            this.disConnectAll();
        }
        if (this.invalidService()) {
            return false;
        }
        return this.bleService.connect(deviceId);
    }

    @Override
    public void connect(List<String> devicesId) {
        this.connect(devicesId, true);
    }

    @Override
    public void connect(List<String> devicesId, boolean increase) {
        if (!increase) {
            this.disConnectAll();
        }
        if (this.invalidService()) {
            return;
        }
        for (String deviceId : devicesId) {
            this.bleService.connect(deviceId);
        }
    }

    @Override
    public void disConnect(String deviceId) {
        if (this.invalidService()) {
            return;
        }
        this.bleService.disconnect(deviceId);
    }

    @Override
    public void disConnect(List<String> devicesId) {
        if (this.invalidService()) {
            return;
        }
        this.bleService.disConnect(devicesId);
    }

    @Override
    public void disConnectAll() {
        if (this.invalidService()) {
            return;
        }
        this.bleService.disConnectAll();
    }

    @Override
    public void syncData(String deviceId) {
        if (this.receiver == null) {
            return;
        }
        this.receiver.sync(deviceId);
    }

    @Override
    public void setConnectListener(OnConnectListener listener) {
        this.connectListener = listener;
    }

    @Override
    public void setToothbrushListener(OnToothbrushDataListener listener) {
        this.dataListener = listener;
    }

    @Override
    public void setMotorListener(OnMotorListener listener) {
        this.motorListener = listener;
    }

    private void onScanError(int code) {
        this.onScanError(code, true);
    }

    private void onScanError(int code, boolean setScannerNull) {
        if (this.scanListener != null) {
            this.scanListener.onScanError(code);
        }
        if (setScannerNull) {
            this.scanBluetooth = null;
        }
    }

    private boolean scanCanCallback() {
        return this.scanListener != null;
    }
}