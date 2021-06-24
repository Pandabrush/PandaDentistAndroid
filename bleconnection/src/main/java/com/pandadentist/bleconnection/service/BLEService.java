package com.pandadentist.bleconnection.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.pandadentist.bleconnection.entity.BluetoothEntity;
import com.pandadentist.bleconnection.entity.ConnectState;
import com.pandadentist.bleconnection.scan.ScanBluetooth;
import com.pandadentist.bleconnection.utils.Logger;
import com.pandadentist.bleconnection.utils.RunTimeLog;
import com.pandadentist.bleconnection.utils.Toasts;
import com.pandadentist.bleconnection.utils.Util;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.pandadentist.bleconnection.entity.ConnectState.CONNECTED;
import static com.pandadentist.bleconnection.entity.ConnectState.CONNECTING;
import static com.pandadentist.bleconnection.entity.ConnectState.DISCONNECTED;

/**
 * CreateTime 2021/6/20 16:48
 * Author zhangwy
 * desc:
 * <p>
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
@SuppressWarnings({"FieldCanBeLocal", "SameParameterValue", "unused", "DanglingJavadoc", "MismatchedQueryAndUpdateOfCollection"})
public class BLEService extends Service {

    private boolean isDestroy = false;
    private final ScanBluetooth scanBluetooth = ScanBluetooth.create();
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallbackImpl(this);
    private final IBinder mBinder = new LocalBinder(this);
    private final HashMap<String, BluetoothEntity> devices = new HashMap<>();
    private final HashSet<String> connectedDevices = new HashSet<>();
    private final HashSet<String> connectingDevices = new HashSet<>();
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private boolean scaning = false;
    private long scanStartTime = -1;
    private long connectStartTime = -1;
    private long disConnectStartTime = -1;

    private final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    public static final String ACTION_GATT_STATE133 = "com.nordicsemi.nrfUART.ACTION_GATT_STATE133";
    public static final String ACTION_GATT_DISCONNECTED = "com.nordicsemi.nrfUART.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.nordicsemi.nrfUART.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_DATA_AVAILABLE = "com.nordicsemi.nrfUART.ACTION_DATA_AVAILABLE";
    public static final String ACTION_GATT_CONNECTED = "com.nordicsemi.nrfUART.ACTION_GATT_CONNECTED";
    public static final String EXTRA_DATA = "com.nordicsemi.nrfUART.EXTRA_DATA";
    public static final String EXTRA_ADDRESS = "com.nordicsemi.nrfUART.EXTRA_ADDRESS";
    public static final String DEVICE_DOES_NOT_SUPPORT_UART = "com.nordicsemi.nrfUART.DEVICE_DOES_NOT_SUPPORT_UART";
//    public static final String DEVICE_REFRESH_FALG = "com.nordicsemi.nrfUART.DEVICE_REFRESH_FALG";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        this.destroy();
        return super.onUnbind(intent);
    }

    public void destroy() {
        this.isDestroy = true;
        this.disConnectAll();
        this.closeAll();
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        Logger.d("initialize");
        this.scanBluetooth.stopLeScan();
        this.onLeScanStop();
        if (!this.scanBluetooth.support()) {
            //TODO
            return false;
        }
        if (this.scanBluetooth.canScan()) {
            //TODO
            return false;
        }
        if (this.scanBluetooth.startLeScan(new OnLeScanListenerImpl(this))) {
            this.scanStartTime = System.currentTimeMillis();
            this.scaning = true;
        } else {
            //TODO
            return false;
        }
        //For API level 18 and above, get a reference to BluetoothAdapter through BluetoothManager.
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Logger.e("Unable to initialize BluetoothManager.");
            return false;
        }

        this.mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Logger.e("Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public void onLeScanStop() {
        RunTimeLog.getInstance(this).log(RunTimeLog.LogAction.SCAN, RunTimeLog.Result.END, "", Util.getUseTime(this.scanStartTime));
        this.scaning = false;
        this.devices.clear();
    }

    public boolean connect(String address) {
        this.connectStartTime = System.currentTimeMillis();
        if (TextUtils.isEmpty(address)) {
            Logger.w("unspecified address.");
            return false;
        }
        if (this.mBluetoothAdapter == null && (this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()) == null) {
            Logger.w("BluetoothAdapter not initialized");
            return false;
        }
        Logger.d(String.format("connect%s", address));
        BluetoothEntity entity = this.devices.get(address);
        if (entity != null && entity.getGatt() != null) {
            if (entity.getGatt().connect()) {
                this.connectedDevices.add(address);
                entity.setState(CONNECTING);
                return true;
            } else {
                return false;
            }
        }

        return this.connectRemoteDevice(address);
    }

    private boolean connectRemoteDevice(String address) {
        if (TextUtils.isEmpty(address)) {
            Logger.w("unspecified address.");
            return false;//failed
        }
        this.connectingDevices.add(address);
        BluetoothEntity entity = this.devices.get(address);
        if (entity != null) {
            Toasts.showShort("取到device，开始连接");
            RunTimeLog.getInstance(this).log(RunTimeLog.LogAction.SCAN, RunTimeLog.Result.SUCCESS, "搜索到设备" + address, Util.getUseTime(this.connectStartTime));
            connectStartTime = System.currentTimeMillis();
            BluetoothDevice device = this.mBluetoothAdapter.getRemoteDevice(address);
            entity.setDevice(device);
            if (device == null) {
                this.devices.remove(address);
                Logger.w("Device not found.  Unable to connect.");
                if (!this.scaning) {
                    this.scanBluetooth.startLeScan(new OnLeScanListenerImpl(this));
                }
                return false;//failed
            }
            this.scanBluetooth.stopLeScan();
//TODO            this.onLeScanStop();
            this.close(address);
            // We want to directly connect to the device, so we are setting the autoConnect parameter to false.
            entity.setGatt(device.connectGatt(this, false, gattCallback));
            Logger.d("Trying to create a new connection.");
            this.connectingDevices.remove(address);
            this.connectedDevices.add(address);
            entity.setState(CONNECTING);
            return true;
        }
        if (!this.scaning) {
            this.scanBluetooth.startLeScan(new OnLeScanListenerImpl(this));
        }
        return false;
    }

    public void disConnectAll() {
        for (String address : this.devices.keySet()) {
            this.disconnect(address);
        }
    }

    public void disConnect(List<String> addresses) {
        if (Util.isEmpty(addresses)) {
            return;
        }
        for (String address : addresses) {
            if (TextUtils.isEmpty(address)) {
                continue;
            }
            this.disconnect(address);
        }
    }

    public void disconnect(String address) {
        this.disConnectStartTime = System.currentTimeMillis();
        Logger.d("disconnect");
        BluetoothEntity entity = this.devices.get(address);
        if (entity == null) {
            Logger.d("device not initialized");
            return;
        }
        BluetoothGatt gatt = entity.getGatt();
        if (gatt == null) {
            Logger.d("BluetoothGatt not initialized");
            return;
        }
        gatt.disconnect();
        entity.setGatt(null);
    }

    public boolean isConnected(String address) {
        BluetoothEntity entity = this.devices.get(address);
        if (entity == null) {
            return false;
        }
        return entity.isConnected();
    }

    public ConnectState getState(String address) {
        BluetoothEntity entity = this.devices.get(address);
        if (entity == null) {
            return ConnectState.UNKNOWN;
        }
        return entity.getState();
    }

    private void setState(String address, ConnectState state) {
        BluetoothEntity entity = this.devices.get(address);
        if (entity != null) {
            entity.setState(state);
        }
        if (state == DISCONNECTED) {
            this.connectedDevices.remove(address);
        }
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    private void closeAll() {
        Logger.d("close");
        this.connectedDevices.clear();
        for (String address : this.devices.keySet()) {
            this.close(address);
        }
    }

    private void close(String address) {
        BluetoothEntity entity = this.devices.get(address);
        if (entity == null) {
            Logger.d("device not initialized");
            return;
        }
        BluetoothGatt gatt = entity.getGatt();
        if (gatt == null) {
            Logger.d("BluetoothGatt not initialized");
            return;
        }
        gatt.close();
        entity.setGatt(null);
        Logger.w("mBluetoothGatt closed");
    }

    private void onLeDevice(BluetoothDevice device) {
        RunTimeLog.getInstance(this).log(RunTimeLog.LogAction.SCAN, RunTimeLog.Result.SUCCESS, device.getAddress() + "-" + device.getName(), Util.getUseTime(scanStartTime));
        String address = device.getAddress();
        if (devices.containsKey(address))
            return;
        devices.put(address, BluetoothEntity.create(device));

        if (this.connectingDevices.contains(address)) {
            this.connectRemoteDevice(address);
        }
    }

    private void disConnectOnDestroy(BluetoothGatt gatt) {
        if (this.isDestroy && gatt != null) {
            gatt.disconnect();
        }
    }

    private void broadcastUpdate(final String action, final String address) {
        Logger.d("broadcastUpdate(final String action)");
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_ADDRESS, address);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final String address, final BluetoothGattCharacteristic characteristic) {
        Logger.d("broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic)");
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (TX_CHAR_UUID.equals(characteristic.getUuid())) {
            intent.putExtra(EXTRA_DATA, characteristic.getValue());
        }
        intent.putExtra(EXTRA_ADDRESS, address);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /***********************************************************************************************/
    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(String address, BluetoothGattCharacteristic characteristic) {
        Logger.d("readCharacteristic");
        BluetoothEntity entity = this.devices.get(address);
        if (entity == null || entity.getGatt() == null) {
            Logger.d("BluetoothGatt not initialized");
            return;
        }
        entity.getGatt().readCharacteristic(characteristic);
    }

    /**
     * Enable TXNotification
     */
    public void enableTXNotification(String address) {
        Logger.d("enableTXNotification");
        BluetoothEntity entity = this.devices.get(address);
        if (entity == null || entity.getGatt() == null) {
            Logger.d("BluetoothGatt not initialized");
            return;
        }

        BluetoothGattService RxService = entity.getGatt().getService(RX_SERVICE_UUID);
        if (RxService == null) {
            Logger.e("Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART, address);
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            Logger.e("Tx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART, address);
            return;
        }
        entity.getGatt().setCharacteristicNotification(TxChar, true);

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        entity.getGatt().writeDescriptor(descriptor);
    }

    public void writeRXCharacteristic(String address, byte[] value) {
        Logger.d("writeRXCharacteristic");
        BluetoothEntity entity = this.devices.get(address);
        if (entity == null || entity.getGatt() == null) {
            Logger.d("BluetoothGatt not initialized");
            return;
        }
        BluetoothGattService RxService = entity.getGatt().getService(RX_SERVICE_UUID);
        Logger.e("mBluetoothGatt null" + entity.getGatt());
        if (RxService == null) {
            Logger.e("Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART, address);
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            Logger.e("Rx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART, address);
            return;
        }
        RxChar.setValue(value);
        boolean status = entity.getGatt().writeCharacteristic(RxChar);
        Logger.d("write TXchar - status=" + status);
    }

    public List<BluetoothGattService> getSupportedGattServices(String address) {
        BluetoothEntity entity = this.devices.get(address);
        return entity == null || entity.getGatt() == null ? null : entity.getGatt().getServices();
    }

    /***********************************************************************************************/

    public static class LocalBinder extends Binder {
        private SoftReference<BLEService> reference;

        private LocalBinder(BLEService service) {
            this.reference = new SoftReference<>(service);
        }

        public BLEService getService() {
            return this.reference == null ? null : this.reference.get();
        }
    }

    private static class OnLeScanListenerImpl implements ScanBluetooth.OnLeScanListener {

        private SoftReference<BLEService> reference;

        private OnLeScanListenerImpl(BLEService service) {
            this.reference = new SoftReference<>(service);
        }

        @Override
        public void onLeDevice(BluetoothDevice device) {
            BLEService service;
            if (this.reference == null || (service = this.reference.get()) == null) {
                return;
            }
            service.onLeDevice(device);
        }
    }

    private static class BluetoothGattCallbackImpl extends BluetoothGattCallback {

        private final int STATUS_133 = 133;
        private final BLEService service;

        private BluetoothGattCallbackImpl(BLEService service) {
            this.service = service;
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Logger.d("address-->" + gatt.getDevice().getAddress() + ";status-->" + status + ";newState-->" + newState);
            String intentAction;
            String address = gatt.getDevice().getAddress();//TODO 赋值是否正确：：从133中移出
            if (status == STATUS_133) {
                Logger.d("onConnectionStateChange received: " + status);
                intentAction = ACTION_GATT_STATE133;//TO DO
                this.service.setState(address, DISCONNECTED);
                this.service.close(address); //TODO 防止出现status 133
                this.service.broadcastUpdate(intentAction, address);
                this.service.connect(address);
                return;
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                RunTimeLog.getInstance(this.service).log(RunTimeLog.LogAction.CONNECT, RunTimeLog.Result.SUCCESS, address, Util.getUseTime(this.service.connectStartTime));
                intentAction = ACTION_GATT_CONNECTED;
                this.service.setState(address, CONNECTED);
                this.service.broadcastUpdate(intentAction, address);
                Logger.i("Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Logger.i("Attempting to start service discovery:" + gatt.discoverServices());
                this.service.disConnectOnDestroy(gatt);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (this.service.disConnectStartTime == -1) {
                    RunTimeLog.getInstance(this.service).log(RunTimeLog.LogAction.DISCONNECT, RunTimeLog.Result.SUCCESS, address + ",被动", 0);
                } else {
                    RunTimeLog.getInstance(this.service).log(RunTimeLog.LogAction.DISCONNECT, RunTimeLog.Result.SUCCESS, address + ",主动", Util.getUseTime(this.service.disConnectStartTime));
                    this.service.disConnectStartTime = -1;
                }
                intentAction = ACTION_GATT_DISCONNECTED;
                this.service.setState(address, DISCONNECTED);
                Logger.i("Disconnected from GATT server.");
                this.service.close(address);
                this.service.broadcastUpdate(intentAction, address);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Logger.d("onServicesDiscovered");
            String address = gatt.getDevice().getAddress();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Logger.e("mBluetoothGatt = " + gatt);
                this.service.broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, address);
            } else {
                Logger.e("onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Logger.d(String.format(Locale.getDefault(), "onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status = %d)", status));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String address = gatt.getDevice().getAddress();
                this.service.broadcastUpdate(ACTION_DATA_AVAILABLE, address, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Logger.d("onCharacteristicChanged");
            String address = gatt.getDevice().getAddress();
            this.service.broadcastUpdate(ACTION_DATA_AVAILABLE, address, characteristic);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Logger.d(String.format(Locale.getDefault(), "onMtuChanged(BluetoothGatt gatt, int mtu = %1$d, int status = %2$d)", mtu, status));
        }
    }
}
