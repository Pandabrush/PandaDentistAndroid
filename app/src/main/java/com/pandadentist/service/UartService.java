package com.pandadentist.service;

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
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.pandadentist.util.Logger;
import com.pandadentist.util.ScanBluetooth;
import com.pandadentist.util.Toasts;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
@SuppressWarnings("unused")
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class UartService extends Service implements ScanBluetooth.OnLeScanListener {

    private boolean isDestroy = false;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private ScanBluetooth scanBluetooth = ScanBluetooth.create();
    private HashMap<String, BluetoothDevice> devices = new HashMap<>();
    private String connectRemoteDeviceAddress;
    private boolean scaning = false;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.nordicsemi.nrfUART.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.nordicsemi.nrfUART.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_STATE133 = "com.nordicsemi.nrfUART.ACTION_GATT_STATE133";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.nordicsemi.nrfUART.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.nordicsemi.nrfUART.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.nordicsemi.nrfUART.EXTRA_DATA";
    public final static String DEVICE_DOES_NOT_SUPPORT_UART = "com.nordicsemi.nrfUART.DEVICE_DOES_NOT_SUPPORT_UART";
    public final static String DEVICE_REFRESH_FALG = "com.nordicsemi.nrfUART.DEVICE_REFRESH_FALG";

    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Logger.d("address-->" + gatt.getDevice().getAddress() + ";status-->" + status + ";newState-->" + newState);
            String intentAction;
            if (status == 133) {
                String address = mBluetoothDeviceAddress;
                Logger.d("onConnectionStateChange received: " + status);
                intentAction = ACTION_GATT_STATE133;//TODO:
                mConnectionState = STATE_DISCONNECTED;//TODO:
                close(); // 防止出现status 133
                broadcastUpdate(intentAction);
                UartService.this.connect(address);
            } else {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = STATE_CONNECTED;
                    broadcastUpdate(intentAction);
                    Logger.i("Connected to GATT server.");
                    // Attempts to discover services after successful connection.
                    Logger.i("Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
                    disConnectOnDestroy(gatt);
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = STATE_DISCONNECTED;
                    Logger.i("Disconnected from GATT server.");
                    close();
                    broadcastUpdate(intentAction);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Logger.d("onServicesDiscovered");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Logger.e("mBluetoothGatt = " + mBluetoothGatt);
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Logger.e("onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Logger.d(String.format(Locale.getDefault(), "onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status = %d)", status));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Logger.d("onCharacteristicChanged");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Logger.d(String.format(Locale.getDefault(), "onMtuChanged(BluetoothGatt gatt, int mtu = %1$d, int status = %2$d)", mtu, status));
        }
    };

    private void disConnectOnDestroy(BluetoothGatt gatt) {
        if (this.isDestroy && gatt != null) {
            gatt.disconnect();
        }
    }

    private void broadcastUpdate(final String action) {
        Logger.d("broadcastUpdate(final String action)");
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        Logger.d("broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic)");
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (TX_CHAR_UUID.equals(characteristic.getUuid())) {
            // Logger.d(String.format("Received TX: %d",characteristic.getValue() ));
            intent.putExtra(EXTRA_DATA, characteristic.getValue());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onLeScanStart() {
        this.scaning = true;
        this.devices.clear();
    }

    @Override
    public void onDevice(BluetoothDevice device) {
        String address = device.getAddress();
        if (devices.containsKey(address))
            return;
        devices.put(address, device);
        if (!TextUtils.isEmpty(this.connectRemoteDeviceAddress)) {
            this.connectRemoteDevice(this.connectRemoteDeviceAddress);
        }
    }

    @Override
    public void onLeScanStop(boolean auto) {
        this.scaning = false;
        if (!auto) {
            this.devices.clear();
        }
    }

    public class LocalBinder extends Binder {
        public UartService getService() {
            return UartService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        disconnect();
        close();
        return super.onUnbind(intent);
    }

    public void destroy() {
        this.isDestroy = true;
        disconnect();
        close();
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        Logger.d("initialize");
        this.scanBluetooth.stopLeScan();
        this.scanBluetooth.startLeScan(null, this);
        //For API level 18 and above, get a reference to BluetoothAdapter through BluetoothManager.
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Logger.e("Unable to initialize BluetoothManager.");
            return false;
        }

        this.mBluetoothAdapter = bluetoothManager.getAdapter();//BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Logger.e("Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void connect(String address) {
        Logger.d("connect");
        if (TextUtils.isEmpty(address)) {
            Logger.w("unspecified address.");
            return;//failed
        }
        if (this.mBluetoothAdapter == null && (this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()) == null) {
            Logger.w("BluetoothAdapter not initialized");
            return;//failed
        }

        // Previously connected device.  Try to reconnect.
        if (!TextUtils.isEmpty(this.mBluetoothDeviceAddress) && TextUtils.equals(address, mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Logger.d("Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return;
            } else {
                return;//failed
            }
        }

        this.connectRemoteDevice(address);
    }

    private void connectRemoteDevice(String address) {
        if (TextUtils.isEmpty(address)) {
            Logger.w("unspecified address.");
            return;//failed
        }
        this.connectRemoteDeviceAddress = address;
        if (this.devices.containsKey(address)) {
            Toasts.showShort("取到device，开始连接");
            BluetoothDevice device = this.mBluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                this.devices.remove(address);
                Logger.w("Device not found.  Unable to connect.");
                if (!this.scaning) {
                    this.scanBluetooth.startLeScan(null, this);
                }
                return;//failed
            }
            this.scanBluetooth.stopLeScan();
            close();
            // We want to directly connect to the device, so we are setting the autoConnect
            // parameter to false.
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
            Logger.d("Trying to create a new connection.");
            this.mBluetoothDeviceAddress = address;
            this.connectRemoteDeviceAddress = "";
            mConnectionState = STATE_CONNECTING;
            return;
        }
        if (!this.scaning) {
            this.scanBluetooth.startLeScan(null, this);
        }
    }

    public int getState() {
        return mConnectionState;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        Logger.d("disconnect");
        if (this.mBluetoothGatt == null) {
            Logger.d("BluetoothGatt not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public boolean hasConnected(String address) {
        BluetoothDevice device;
        return this.mBluetoothGatt != null && (device = this.mBluetoothGatt.getDevice()) != null && TextUtils.equals(address, device.getAddress()) && TextUtils.equals(address, mBluetoothDeviceAddress);
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        Logger.d("close");
        if (mBluetoothGatt == null) {
            return;
        }
        Logger.w("mBluetoothGatt closed");
        mBluetoothDeviceAddress = null;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        Logger.d("readCharacteristic");
        if (mBluetoothGatt == null) {
            Logger.d("BluetoothGatt not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

//    /**
//     * Enables or disables notification on a give characteristic.
//     *
//     * @param characteristic Characteristic to act on.
//     * @param enabled If true, enable notification.  False otherwise.
//     */
//    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Logger.w("BluetoothAdapter not initialized");
//            return;
//        }
//        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
//        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(descriptor);
//        }
//    }

    /**
     * Enable TXNotification
     */
    public void enableTXNotification() {
        Logger.d("enableTXNotification");
        if (mBluetoothGatt == null) {
            Logger.d("BluetoothGatt not initialized");
            return;
        }

        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        if (RxService == null) {
            showMessage("Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            showMessage("Tx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar, true);

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);

    }

    public void writeRXCharacteristic(byte[] value) {
        Logger.d("writeRXCharacteristic");
        if (this.mBluetoothGatt == null) {
            Logger.d("BluetoothGatt not initialized");
            return;
        }
        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        showMessage("mBluetoothGatt null" + mBluetoothGatt);
        if (RxService == null) {
            showMessage("Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            showMessage("Rx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        RxChar.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(RxChar);

        Logger.d("write TXchar - status=" + status);
    }

    private void showMessage(String msg) {
        Logger.e(msg);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        return mBluetoothGatt == null ? null : mBluetoothGatt.getServices();
    }
}
