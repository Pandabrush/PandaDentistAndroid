package com.pandadentist.bleconnection.entity;

/**
 * CreateTime 2021/6/20 14:11
 * Author zhangwy
 * desc:
 * <p>
 * -------------------------------------------------------------------------------------------------
 * use:
 **/

public class BleTypeEntity extends BaseEntity {
    private String prefix;//前缀
    private String title;
    private boolean isChild;
    private boolean isCyq;
    private int type;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isChild() {
        return isChild;
    }

    public void setChild(boolean child) {
        isChild = child;
    }

    public boolean isCyq() {
        return isCyq;
    }

    public void setCyq(boolean cyq) {
        isCyq = cyq;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
