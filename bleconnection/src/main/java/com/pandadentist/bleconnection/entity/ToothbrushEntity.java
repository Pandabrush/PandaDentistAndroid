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
    private int time;

    public String getDeviceid() {
        return deviceid;
    }

    public String getContent() {
        return content;
    }

    public int getTime() {
        return time;
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private String deviceid;
        private String content;
        private int time;

        public Builder setDeviceid(String deviceid) {
            this.deviceid = deviceid;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setTime(int time) {
            this.time = time;
            return this;
        }

        public ToothbrushEntity build() {
            ToothbrushEntity entity = new ToothbrushEntity();
            entity.deviceid = this.deviceid;
            entity.content = this.content;
            entity.time = this.time;
            return entity;
        }
    }
}
