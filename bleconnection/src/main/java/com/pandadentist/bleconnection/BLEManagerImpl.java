package com.pandadentist.bleconnection;

import android.bluetooth.BluetoothDevice;

import com.pandadentist.bleconnection.scan.ScanBluetooth;

/**
 * CreateTime 2021/6/20 14:27
 * Author zhangwy
 * desc:
 * <p>
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
public class BLEManagerImpl extends BLEManager implements ScanBluetooth.OnLeScanListener {

    private ScanBluetooth scanBluetooth;
    private OnScanListener scanListener;

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
    }

    @Override
    public void binding(String deviceId) {
    }

    @Override
    public void connect() {
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