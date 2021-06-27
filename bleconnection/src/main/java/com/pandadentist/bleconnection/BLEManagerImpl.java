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
import android.text.TextUtils;

import com.pandadentist.bleconnection.entity.RunTimeEntity;
import com.pandadentist.bleconnection.entity.ToothbrushEntity;
import com.pandadentist.bleconnection.entity.ToothbrushInfoEntity;
import com.pandadentist.bleconnection.entity.ToothbrushModeType;
import com.pandadentist.bleconnection.entity.ToothbrushSettingConfigEntity;
import com.pandadentist.bleconnection.entity.ToothbrushSettingEntity;
import com.pandadentist.bleconnection.interaction.Command;
import com.pandadentist.bleconnection.interaction.Send2Buffer;
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
    private OnToothbrushSettingListener settingListener;
    private OnToothbrushSetErrorAlertListener errorAlertListener;
    private OnToothbrushRuntimeListener runtimeListener;
    private String lastRuntimeDevice;
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
    public void onConnected(String address) {
        if (this.connectListener == null) {
            return;
        }
        this.connectListener.onConnected(address);
    }

    @Override
    public void onDisconnected(String deviceId) {
        if (this.connectListener == null) {
            return;
        }
        this.connectListener.onDisConnected(deviceId);
    }

    @Override
    public void onServicesDiscovered(String deviceId) {
        if (this.invalidService()) {
            return;
        }
        this.bleService.enableTXNotification(deviceId);
    }

    @Override
    public void onBleNonSupport(String deviceId) {
        if (this.connectListener == null) {
            return;
        }
        this.connectListener.onConnectError(deviceId, Content.CODE_ERROR_BLE_NONSUPPORT);
    }

    @Override
    public void onReadStart(String deviceId) {
        if (this.dataListener == null) {
            return;
        }
        this.dataListener.onReadStart(deviceId);
    }

    @Override
    public void onRead(String deviceId, ToothbrushInfoEntity infoEntity, ToothbrushEntity dataEntity) {
        if (this.dataListener == null) {
            return;
        }
        this.dataListener.onData(deviceId, infoEntity, dataEntity);
    }

    private void write(String deviceId, Command command) {
        this.onWrite(deviceId, command.command);
    }

    @Override
    public void onWrite(String deviceId, byte[] bytes) {
        if (this.invalidService()) {
            return;
        }
        this.bleService.writeRXCharacteristic(deviceId, bytes);
    }

    @Override
    public void onNoData(String deviceId) {
        if (this.dataListener == null) {
            return;
        }
        this.dataListener.onNoData(deviceId);
    }

    @Override
    public void onRuntime(String deviceId, RunTimeEntity runTimeEntity) {
        if (this.runtimeListener == null) {
            return;
        }
        this.runtimeListener.onToothbrushRuntime(deviceId, runTimeEntity);
    }

    @Override
    public void onSettingInfo(String deviceId, ToothbrushSettingEntity settingEntity) {
        if (this.settingListener == null) {
            return;
        }
        this.settingListener.onToothbrushSettingInfo(deviceId, settingEntity);
    }

    /**
     * 设置错误警告牙刷返回配置信息，所以该回调的逻辑同时处理错误警告的回调，
     *
     * @param deviceId     牙刷地址
     * @param configEntity 牙刷设置配置对象
     */
    @Override
    public void onSettingConfig(String deviceId, ToothbrushSettingConfigEntity configEntity) {
        if (this.errorAlertListener != null) {
            this.errorAlertListener.onErrorAlertValue(deviceId, configEntity.isStandardBash(), configEntity.isStandardGb());
            this.errorAlertListener = null;
            return;
        }
        if (this.settingListener != null) {
            this.settingListener.onToothbrushSettingConfig(deviceId, configEntity);
        }
    }

    private boolean invalidService() {
        return this.bleService == null;
    }

    @Override
    public void destroy() {
        this.stopScan();
        this.stopRuntime(this.lastRuntimeDevice);
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
        this.bindContext = null;
        this.scanListener = null;
        this.setConnectListener(null);
        this.errorAlertListener = null;
        this.setToothbrushListener(null);
        this.settingListener = null;
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
    public void reqData(String deviceId) {
        this.write(deviceId, Command.REQ_DATA);
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
    public void getToothbrushSettingConfig(String deviceId) {
        this.write(deviceId, Command.SETTING_CONFIG);
    }

    @Override
    public void setToothbrushSettingListener(OnToothbrushSettingListener listener) {
        this.settingListener = listener;
    }

    @Override
    public void getToothbrushSettingInfo(String deviceId) {
        this.write(deviceId, Command.SETTING_VALUE);
    }

    @Override
    public void setToothbrush(String deviceId, ToothbrushModeType modeType, int mode, int pwm, int tclk, int time) {
        this.onWrite(deviceId, Send2Buffer.setting(modeType, mode, pwm, tclk, time));
    }

    @Override
    public void setErrorAlert(String deviceId, boolean bash, boolean gb, OnToothbrushSetErrorAlertListener listener) {
        this.errorAlertListener = listener;
        this.write(deviceId, Command.ERROR_ALERT.errorAlert(bash, gb));
    }

    @Override
    public void settingFinish(String deviceId) {
        this.write(deviceId, Command.CALLBACK);
        this.settingListener = null;
        this.errorAlertListener = null;
    }

    @Override
    public void startRuntime(String deviceId, OnToothbrushRuntimeListener listener) {
        this.stopRuntime(this.lastRuntimeDevice);
        this.lastRuntimeDevice = deviceId;
        this.write(deviceId, Command.RUNTIME);
    }

    @Override
    public void stopRuntime(String deviceId) {
        if (TextUtils.isEmpty(deviceId) || !TextUtils.equals(deviceId, this.lastRuntimeDevice)) {
            return;
        }
        this.write(deviceId, Command.CALLBACK);
        this.runtimeListener = null;
        this.lastRuntimeDevice = null;
    }

    @Override
    public void adjusting(String deviceId) {
        this.write(deviceId, Command.ADJUSTING);
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