package com.pandadentist.bleconnection.entity;

/**
 * CreateTime 2021/6/27 11:45
 * Author zhangwy
 * desc:
 * 电机配置范围信息
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
@SuppressWarnings("unused")
public class ToothbrushSettingConfigEntity extends BaseEntity {
    private String deviceId;
    private boolean standardBash;//电机异常振动警告
    private boolean standardGb;//电机异常振动警告
    private boolean canSetting;
    private int basicModelNum;//牙刷基础刷牙模式数量 对应num_def
    private int maxTClk;//最大振动周期
    private int minTClk;//最小振动周期
    private int maxPwm;//最大占空比
    private int minPwn;//最小占空比
    private int maxTime;//最大刷牙时长
    private int minTime;//最小刷牙时长

    public String getDeviceId() {
        return deviceId;
    }

    public boolean isStandardBash() {
        return standardBash;
    }

    public boolean isStandardGb() {
        return standardGb;
    }

    public boolean isCanSetting() {
        return canSetting;
    }

    public int getBasicModelNum() {
        return basicModelNum;
    }

    public int getMaxTClk() {
        return maxTClk;
    }

    public int getMinTClk() {
        return minTClk;
    }

    public int getMaxPwm() {
        return maxPwm;
    }

    public int getMinPwn() {
        return minPwn;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public int getMinTime() {
        return minTime;
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private String deviceId;
        private boolean standardBash;//电机异常振动警告
        private boolean standardGb;//电机异常振动警告
        private boolean canSetting;
        private int basicModelNum;//牙刷基础刷牙模式数量 对应num_def
        private int maxTClk;//最大振动周期
        private int minTClk;//最小振动周期
        private int maxPwm;//最大占空比
        private int minPwn;//最小占空比
        private int maxTime;//最大刷牙时长
        private int minTime;//最小刷牙时长

        public Builder setDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder setStandardBash(boolean standardBash) {
            this.standardBash = standardBash;
            return this;
        }

        public Builder setStandardGb(boolean standardGb) {
            this.standardGb = standardGb;
            return this;
        }

        public Builder setCanSetting(boolean canSetting) {
            this.canSetting = canSetting;
            return this;
        }

        public Builder setBasicModelNum(int basicModelNum) {
            this.basicModelNum = basicModelNum;
            return this;
        }

        public Builder setMaxTClk(int maxTClk) {
            this.maxTClk = maxTClk;
            return this;
        }

        public Builder setMinTClk(int minTClk) {
            this.minTClk = minTClk;
            return this;
        }

        public Builder setMaxPwm(int maxPwm) {
            this.maxPwm = maxPwm;
            return this;
        }

        public Builder setMinPwn(int minPwn) {
            this.minPwn = minPwn;
            return this;
        }

        public Builder setMaxTime(int maxTime) {
            this.maxTime = maxTime;
            return this;
        }

        public Builder setMinTime(int minTime) {
            this.minTime = minTime;
            return this;
        }

        public ToothbrushSettingConfigEntity build() {
            ToothbrushSettingConfigEntity entity = new ToothbrushSettingConfigEntity();
            entity.deviceId = this.deviceId;
            entity.standardBash = this.standardBash;
            entity.standardGb = this.standardGb;
            entity.canSetting = this.canSetting;
            entity.basicModelNum = this.basicModelNum;
            entity.maxTClk = this.maxTClk;
            entity.minTClk = this.minTClk;
            entity.maxPwm = this.maxPwm;
            entity.minPwn = this.minPwn;
            entity.maxTime = this.maxTime;
            entity.minTime = this.minTime;
            return entity;
        }
    }
}
