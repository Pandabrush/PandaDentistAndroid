package com.pandadentist.bleconnection.entity;

/**
 * CreateTime 2021/6/27 12:07
 * Author zhangwy
 * desc:
 * 电机当前设置信息
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
@SuppressWarnings({"unused"})
public class ToothbrushSettingEntity extends BaseEntity {
    private int modeType;
    private int mode;
    private int pwm;
    private int tclk;
    private int time;
    private int status;//0:设置成功，1：tclk超出范围；2：pwm超出范围；3：Time超出范围；4：pwm>tclk/2；0xf0：不支持自定义设置

    public ToothbrushModeType getModeType() {
        return ToothbrushModeType.find(this.modeType);
    }

    public int getMode() {
        return mode;
    }

    public int getPwm() {
        return pwm;
    }

    public int getTclk() {
        return tclk;
    }

    public int getTime() {
        return time;
    }

    public int getStatus() {
        return status;
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private int modeType;
        private int mode;
        private int pwm;
        private int tclk;
        private int time;
        private int status;

        public Builder setModeType(ToothbrushModeType modeType) {
            this.modeType = modeType.code;
            return this;
        }

        public Builder setModeType(int modeType) {
            this.modeType = modeType;
            return this;
        }

        public Builder setMode(int mode) {
            this.mode = mode;
            return this;
        }

        public Builder setPwm(int pwm) {
            this.pwm = pwm;
            return this;
        }

        public Builder setTclk(int tclk) {
            this.tclk = tclk;
            return this;
        }

        public Builder setTime(int time) {
            this.time = time;
            return this;
        }

        public Builder setStatus(int status) {
            this.status = status;
            return this;
        }

        public ToothbrushSettingEntity build() {
            ToothbrushSettingEntity entity = new ToothbrushSettingEntity();
            entity.modeType = this.modeType;
            entity.mode = this.mode;
            entity.pwm = this.pwm;
            entity.tclk = this.tclk;
            entity.time = this.time;
            entity.status = this.status;
            return entity;
        }
    }
}
