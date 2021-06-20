package com.pandadentist.bleconnection.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.pandadentist.bleconnection.entity.BleTypeEntity;
import com.pandadentist.bleconnection.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

    public abstract boolean support();

    /**
     * 是否能扫描
     *
     * @return true 可以执行搜索 反之不能执行搜索
     */
    public abstract boolean canScan();

    public abstract boolean isScanning();

    public abstract void setListener(OnLeScanListener listener);

    public abstract boolean startLeScan(OnLeScanListener listener);

    public abstract void stopLeScan();

    private static class ScanBluetoothImpl extends ScanBluetooth implements BluetoothAdapter.LeScanCallback, Handler.Callback {
        private final String bleTypeJson = "{\"PBRUSH\":{\"title\":\"熊猫刷牙-智能牙刷(默认)\",\"ischild\":false,\"iscyq\":false,\"type\":1},\"Pbrush\":{\"title\":\"熊猫刷牙-二代智能牙刷\",\"ischild\":false,\"iscyq\":false,\"type\":102},\"PBrush\":{\"title\":\"熊猫刷牙-智能牙刷\",\"ischild\":false,\"iscyq\":false,\"type\":101},\"PBRush\":{\"title\":\"熊猫刷牙-儿童牙刷\",\"ischild\":true,\"iscyq\":false,\"type\":110},\"PBCYQX\":{\"title\":\"熊猫刷牙-智能冲牙器\",\"ischild\":false,\"iscyq\":true,\"type\":201},\"pBrush\":{\"title\":\"熊猫刷牙-智能冲牙器\",\"ischild\":false,\"iscyq\":true,\"type\":202}}";
        private final List<BleTypeEntity> bleTypeEntities = new ArrayList<>();

        private final int WHAT_ADD_DEVICE = 100;
        private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        private Handler handler = new Handler(Looper.getMainLooper(), this);
        private HashMap<String, BluetoothDevice> deviceHashMap = new HashMap<>();
        private OnLeScanListener listener;
        private boolean scaning = false;

        private ScanBluetoothImpl() {
            try {
                JSONObject json = new JSONObject(bleTypeJson);
                Iterator<String> iterator = json.keys();
                while (iterator != null && iterator.hasNext()) {
                    String key = iterator.next();
                    JSONObject typeJson = json.optJSONObject(key);
                    if (typeJson == null) {
                        continue;
                    }
                    BleTypeEntity entity = new BleTypeEntity();
                    entity.setPrefix(key);
                    entity.setTitle(typeJson.optString("title", ""));
                    entity.setChild(typeJson.optBoolean("ischild", false));
                    entity.setCyq(typeJson.optBoolean("iscyq", false));
                    entity.setType(typeJson.optInt("type", 0));
                    if (entity.getType() < 0 || TextUtils.isEmpty(entity.getPrefix())) {
                        continue;
                    }
                    bleTypeEntities.add(entity);
                }
            } catch (JSONException e) {
                Logger.e("parseBleTypeJson", e);
            }
        }

        @Override
        public boolean support() {
            if (this.adapter == null) {
                this.adapter = BluetoothAdapter.getDefaultAdapter();
            }

            return this.adapter != null;
        }

        @Override
        public boolean canScan() {
            if (!this.support()) {
                return false;
            }
            return this.adapter.isEnabled();
        }

        @Override
        public boolean isScanning() {
            return this.scaning;
        }

        @Override
        public boolean startLeScan(OnLeScanListener listener) {
            this.setListener(listener);
            return this.canScan() && this.startLeScan();
        }

        public void setListener(OnLeScanListener listener) {
            this.listener = listener;
        }

        private boolean startLeScan() {
            this.deviceHashMap.clear();
            return (this.scaning = this.adapter.startLeScan(this));
        }

        @Override
        public void stopLeScan() {
            this.finalStopLeScan();
        }

        private void finalStopLeScan() {
            this.scaning = false;
            if (this.adapter != null) {
                this.adapter.stopLeScan(this);
            }
        }

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device == null)
                return;
            String name = device.getName();
            Logger.d(String.format(Locale.getDefault(), "addDevices:%1$s", name));
            if (TextUtils.isEmpty(name)) {
                return;
            }
            for (BleTypeEntity entity : this.bleTypeEntities) {
                if (name.startsWith(entity.getPrefix())) {
                    this.handler.sendMessage(this.handler.obtainMessage(WHAT_ADD_DEVICE, device));
                    break;
                }
            }
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == WHAT_ADD_DEVICE) {
                this.addDevice((BluetoothDevice) msg.obj);
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
                this.listener.onLeDevice(device);
            }
        }

        private boolean canCallback() {
            return this.listener != null;
        }
    }

    public interface OnLeScanListener {
        void onLeDevice(BluetoothDevice device);
    }
}
