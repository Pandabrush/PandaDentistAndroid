package com.pandadentist.bleconnection.entity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.text.TextUtils;

/**
 * CreateTime 2021/6/20 18:24
 * Author zhangwy
 * desc:
 * <p>
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
@SuppressWarnings("unused")
public class BluetoothEntity extends BaseEntity {
    private String address;
    private BluetoothDevice device;
    private BluetoothGatt gatt;
    private ConnectState state;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }

    public void setGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    public ConnectState getState() {
        return state;
    }

    public void setState(ConnectState state) {
        this.state = state;
    }

    public boolean isConnected() {
        if (this.getDevice() == null || TextUtils.equals(this.getAddress(), this.getDevice().getAddress())) {
            return false;
        }
        BluetoothDevice device;
        return this.getGatt() != null && (device = this.getGatt().getDevice()) != null && TextUtils.equals(device.getAddress(), this.getAddress());
//      return this.mBluetoothGatt != null && (device = this.mBluetoothGatt.getDevice()) != null && TextUtils.equals(address, device.getAddress()) && TextUtils.equals(address, mBluetoothDeviceAddress);
    }

    public static BluetoothEntity create(BluetoothDevice device) {
        if (device == null) {
            return null;
        }
        BluetoothEntity entity = new BluetoothEntity();
        entity.setAddress(device.getAddress());
        entity.setDevice(device);
        return entity;
    }
}
