package com.pandadentist.bleconnection.interaction;

import com.pandadentist.bleconnection.entity.ToothbrushModeType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.List;

/**
 * CreateTime 2021/6/27 14:28
 * Author zhangwy
 * desc:
 * 数据转成Buffer
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess", "unused", "deprecation"})
public class Send2Buffer {
    //APP发送给设备的指令
    private static final int COMMAND_REQ_DATA = 0;             //请求过程
    private static final int COMMAND_REQ_RESULT = 1;           //请求结果
    private static final int COMMAND_DATA_MISSED = 0x10;       //丢失帧
    private static final int COMMAND_REQ_RUNTIME = 20;         //实时数据请求
    private static final int COMMAND_CALLBACK = 0x21;          //设置完成返回
    private static final int COMMAND_SETTING = 0x80;           //设置电机参数
    private static final int COMMAND_AUTO_RESTART = 0x81;      //自启动配置
    private static final int COMMAND_ERR_MOT = 0x82;           //电机报警姿态输出
    private static final int COMMAND_SETTING_CONFIG = 0x83;    //电机配置
    private static final int COMMAND_DEVICE_INFO = 0xa0;       //获取设备信息
    private static final int COMMAND_ADJUST = 0xb0;            //校准指令
    private static final int COMMAND_UPLOAD = 0xc0;            //升级指令
    private static final int COMMAND_COMPLETED = 0xf0;         //传输完成

    /**
     * 获取设备信息
     *
     * @return {}
     */
    public static byte[] deviceInfo() {
        ByteBuffer data = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) COMMAND_DEVICE_INFO);
        data.put((byte) 0);
        return data.array();
    }

    /**
     * 设备校准
     *
     * @return {}
     */
    public static byte[] adjusting() {
        ByteBuffer data = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) COMMAND_ADJUST);
        data.put((byte) 0);
        return data.array();
    }

    /**
     * 请求实时动画
     *
     * @return {}
     */
    public static byte[] runtime() {
        ByteBuffer data = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) COMMAND_REQ_RUNTIME);
        data.put((byte) 1);
        data.putShort((short) 0);
        return data.array();
    }

    public static byte[] dataReq() {
        ByteBuffer data = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        Date date = new Date(System.currentTimeMillis());
        data.put((byte) (COMMAND_REQ_RESULT));
        data.put((byte) 0);
        data.putShort((short) 0); //2bytes 默认
        data.put((byte) date.getSeconds());
        data.put((byte) date.getMinutes());
        data.put((byte) date.getHours());
        data.put((byte) 0x0);
        data.put((byte) date.getDate());
        data.put((byte) (date.getMonth() + 1));                 //0 开�?
        data.putShort((short) (date.getYear() - 100 + 2000));  //1900开�?
        //时间换算为从 1970-1-1 00:00:00 到现在的秒数
        data.putInt((int) (date.getTime() / 1000));   //+时区 cn=8:00:00
        return data.array();
    }

    /**
     * 丢失帧请求
     *
     * @param missIndex 为丢失帧的向量组合
     * @return {}
     */
    public static byte[] dataRemap(List<Integer> missIndex) {
        int missedPackageCount = Math.min(missIndex.size(), 8);
        ByteBuffer data = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) COMMAND_DATA_MISSED);
        data.put((byte) missedPackageCount);
        data.putShort((short) missIndex.size());
        for (int index = 0; index < missedPackageCount; index++) {
            data.putShort(missIndex.get(index).shortValue());
        }
        return data.array();
    }

    public static byte[] complete() { //重传请求
        ByteBuffer data = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) COMMAND_COMPLETED);
        return data.array();
    }

    /**
     * 重传请求
     * -------对内测试
     *
     * @param num      :丢失帧数
     * @param totalnum :总帧数
     * @param index    :丢失帧帧数列表
     * @return {}
     */
    @Deprecated
    public static byte[] remap(int num, int totalnum, int[] index) {
        ByteBuffer data = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) COMMAND_DATA_MISSED);
        data.put((byte) num);
        data.putShort((short) (totalnum));
        for (int i = 0; i < num; i++) {
            data.putShort((short) (index[i]));
        }
        return data.array();
    }

    /**
     * 固件升级
     * cmd ：升级  0xfc强制升级， 0xfe同版本可降级，其他-判断版本号
     * versionX : 版本 versionS 是软件版本，同版本的时候，默认其它相等 versionS>当前才能升级
     */
    @Deprecated
    public static byte[] firmwareUpdate(int cmd, int versionS, int versionH, int versionF, int versionM) {
        ByteBuffer data = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) COMMAND_UPLOAD);
        data.put((byte) cmd); //fc强制升级， fe同版本可降级，其他-判断版本号
        data.putShort((short) 0);
        data.putShort((short) (versionS));
        data.putShort((short) (versionH));
        data.putShort((short) (versionF));
        data.putShort((short) (versionM));

        for (int i = 12; i < 20; i++) {
            data.put((byte) 0);
        }
        return data.array();
    }

    public static byte[] settingConfig() {
        ByteBuffer data = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) COMMAND_SETTING_CONFIG);
        return data.array();
    }

    /**
     * 设置
     * ---先收到当前状态信息
     * ---后收到配置范围
     *
     * @param modeType 设置内容的方式， 0-按档位设定	1-只改变周期	2-改变幅度	3-只改变时间，4-同时改变周期和幅度和时间，0xFF获取当前状态值
     * @param mode     0-5 档位值， 当按档位设定时， 这个代表运行的档位。
     * @param pwm      范围 200 - 1800  与tclk 一起组合 确定牙刷的振动参数
     * @param tclk     范围(2000(高频)->6000(低频))	默认3800对应(31000次/min)
     * @param time     刷牙时间 	单位s
     * @return {}
     */
    public static byte[] setting(ToothbrushModeType modeType, int mode, int pwm, int tclk, int time) {
        ByteBuffer data = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) COMMAND_SETTING);
        data.put((byte) modeType.code);
        data.putShort((short) mode);
        data.putShort((short) pwm);
        data.putShort((short) tclk);
        data.putShort((short) time);
        for (int i = 10; i < 20; i++) {
            data.put((byte) 0);
        }
        return data.array();
    }

    public static byte[] settingValue() {
        return setting(ToothbrushModeType.VALUE, 0, 0, 0, 0);
    }

    /**
     * 退出设置界面、刷牙指导界面
     *
     * @return {}
     */
    public static byte[] callBack() {
        ByteBuffer data = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) COMMAND_CALLBACK);
        data.put((byte) 0);
        data.putShort((short) 0);
        return data.array();
    }

    /**
     * 设备自启动运行，测试模式
     *
     * @param auto     1:start  0:stop
     * @param autoTime 自启动时间
     * @param runtime  每次启动运行时间
     * @param times    自启动的次数。
     * @return {}
     */
    @Deprecated
    public static byte[] testAutoRestart(int auto, int autoTime, int runtime, int times) { //设置自动启动周期
        ByteBuffer data = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) COMMAND_AUTO_RESTART);
        data.put((byte) 1);
        data.putShort((short) (1));
        data.putShort((short) (auto));
        data.putShort((short) (autoTime));
        data.putShort((short) (runtime));
        data.putShort((short) (times));
        for (int i = 12; i < 20; i++)
            data.put((byte) 0);
        return data.array();
    }

    /**
     * 错误报警设置
     *
     * @param bash ”
     * @param gb   “
     * @return {}
     */
    public static byte[] errAlert(boolean bash, boolean gb) { //设置错误报警
        ByteBuffer data = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) COMMAND_ERR_MOT);
        data.put((byte) (bash ? 1 : 0));
        data.put((byte) (gb ? 0x0f : 0x7)); //默认false:不加水平震颤检测，
        return data.array();
    }
}