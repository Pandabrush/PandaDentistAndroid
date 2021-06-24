package com.pandadentist.bleconnection.entity;

/**
 * CreateTime 2021/6/25 1:00
 * Author zhangwy
 * desc:
 * <p>
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
@SuppressWarnings("unused")
public class ToothbrushEntity extends BaseEntity {
    private String deviceid;
    private String content;
    private int software;
    private int factory;
    private int model;
    private int power;
    private int time;
    private int hardware;
    private int dataTyp;
    private int pageSize;

    public String getDeviceid() {
        return deviceid;
    }

    public String getContent() {
        return content;
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

    public int getTime() {
        return time;
    }

    public int getHardware() {
        return hardware;
    }

    public int getDataTyp() {
        return dataTyp;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private String deviceid;
        private String content;
        private int software;
        private int factory;
        private int model;
        private int power;
        private int time;
        private int hardware;
        private int dataTyp;
        private int pageSize;

        public Builder setDeviceid(String deviceid) {
            this.deviceid = deviceid;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
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

        public Builder setTime(int time) {
            this.time = time;
            return this;
        }

        public Builder setHardware(int hardware) {
            this.hardware = hardware;
            return this;
        }

        public Builder setDataTyp(int dataTyp) {
            this.dataTyp = dataTyp;
            return this;
        }

        public Builder setPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public ToothbrushEntity build() {
            ToothbrushEntity entity = new ToothbrushEntity();
            entity.deviceid = this.deviceid;
            entity.content = this.content;
            entity.software = this.software;
            entity.factory = this.factory;
            entity.model = this.model;
            entity.power = this.power;
            entity.time = this.time;
            entity.hardware = this.hardware;
            entity.dataTyp = this.dataTyp;
            entity.pageSize = this.pageSize;
            return entity;
        }
    }
}
