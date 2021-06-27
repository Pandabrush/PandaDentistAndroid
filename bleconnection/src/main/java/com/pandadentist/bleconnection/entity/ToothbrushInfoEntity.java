package com.pandadentist.bleconnection.entity;

/**
 * CreateTime 2021/6/27 15:55
 * Author zhangwy
 * desc:
 * 牙刷信息
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
@SuppressWarnings("unused")
public class ToothbrushInfoEntity extends BaseEntity {
    private String deviceid;
    private String content;
    private int hardware;
    private int software;
    private int factory;
    private int model;
    private int power;

    public String getDeviceid() {
        return deviceid;
    }

    public String getContent() {
        return content;
    }

    public int getHardware() {
        return hardware;
    }

    public int getSoftware() {
        return software;
    }

    public int getFactory() {
        return factory;
    }

    public int getModel() {
        return model;
    }

    public int getPower() {
        return power;
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private String deviceid;
        private int hardware;
        private int software;
        private int factory;
        private int model;
        private int power;

        public Builder setDeviceid(String deviceid) {
            this.deviceid = deviceid;
            return this;
        }

        public Builder setHardware(int hardware) {
            this.hardware = hardware;
            return this;
        }

        public Builder setSoftware(int software) {
            this.software = software;
            return this;
        }

        public Builder setFactory(int factory) {
            this.factory = factory;
            return this;
        }

        public Builder setModel(int model) {
            this.model = model;
            return this;
        }

        public Builder setPower(int power) {
            this.power = power;
            return this;
        }

        public ToothbrushInfoEntity build() {
            ToothbrushInfoEntity entity = new ToothbrushInfoEntity();
            entity.deviceid = this.deviceid;
            entity.hardware = this.hardware;
            entity.software = this.software;
            entity.factory = this.factory;
            entity.model = this.model;
            entity.power = this.power;
            return entity;
        }
    }
}
