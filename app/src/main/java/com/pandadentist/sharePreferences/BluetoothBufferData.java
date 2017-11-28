package com.pandadentist.sharePreferences;

import android.content.Context;

/**
 * Created by zhangwy on 2017/11/15.
 * Updated by zhangwy on 2017/11/15.
 * Description 蓝牙缓存数据
 */
@SuppressWarnings("unused")
public abstract class BluetoothBufferData {
    private static BluetoothBufferData instance;

    static {
        instance = new BluetoothBufferDataImpl();
    }

    public static BluetoothBufferData getInstance() {
        return instance;
    }

    public abstract void init(Context context);

    public abstract void add(BufferData data);

    public abstract void delete(BufferData data);

    public abstract BufferData get();

    /**********************************************************************************************/
    private static class BluetoothBufferDataImpl extends BluetoothBufferData {
        @Override
        public void init(Context context) {
            this.getHelper().init(context);
        }

        @Override
        public void add(BufferData data) {
            PreferencesHelper helper = this.getHelper();
        }

        @Override
        public void delete(BufferData data) {

        }

        @Override
        public BufferData get() {
            return null;
        }

        private PreferencesHelper getHelper() {
            return PreferencesHelper.newInstance("BluetoothBufferData");
        }
    }

    public static class BufferData {
        private String deviceid;
        private String software;
        private String factory;
        private String model;
        private String power;
        private String time;
        private String hardware;
        private String content;
        private String dataType;

        public BufferData(String deviceid, int software, int factory, int model, int power, int time, int hardware, String content, int dataType) {
            this.deviceid = deviceid;
            this.software = String.valueOf(software);
            this.factory = String.valueOf(factory);
            this.model = String.valueOf(model);
            this.power = String.valueOf(power);
            this.time = String.valueOf(time);
            this.hardware = String.valueOf(hardware);
            this.content = content;
            this.dataType = String.valueOf(dataType);
        }

        public String getDeviceid() {
            return deviceid;
        }

        public String getSoftware() {
            return software;
        }

        public String getFactory() {
            return factory;
        }

        public String getModel() {
            return model;
        }

        public String getPower() {
            return power;
        }

        public String getTime() {
            return time;
        }

        public String getHardware() {
            return hardware;
        }

        public String getContent() {
            return content;
        }

        public String getDataType() {
            return dataType;
        }
    }
}
