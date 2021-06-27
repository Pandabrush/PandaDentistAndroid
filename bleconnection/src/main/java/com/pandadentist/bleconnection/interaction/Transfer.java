package com.pandadentist.bleconnection.interaction;

import com.pandadentist.bleconnection.entity.RunTimeEntity;
import com.pandadentist.bleconnection.entity.ToothbrushEntity;
import com.pandadentist.bleconnection.entity.ToothbrushInfoEntity;
import com.pandadentist.bleconnection.entity.ToothbrushSettingConfigEntity;
import com.pandadentist.bleconnection.entity.ToothbrushSettingEntity;
import com.pandadentist.bleconnection.utils.Bytes;
import com.pandadentist.bleconnection.utils.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * CreateTime 2021/6/27 14:26
 * Author zhangwy
 * desc:
 * 接收蓝牙数据解析
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
@SuppressWarnings({"ConstantConditions", "DuplicateBranchesInSwitch", "FieldCanBeLocal", "WeakerAccess", "PointlessBitwiseExpression", "IncompatibleBitwiseMaskOperation", "SwitchStatementWithoutDefaultBranch", "unused", "DanglingJavadoc", "UnusedReturnValue"})
public class Transfer {
    private final int LEN_FRAME = 20;
    //传输的状态
    private final byte UP_STA_IDLE = 0;        //空闲状态
    private final byte UP_STA_START = 1;       //开始传数据
    private final byte UP_STA_RESEND = 2;      //重传中
    private final byte UP_STA_COMPLETE = 3;    //当前条传输完毕
    private final byte UP_STA_TIMEOUT = 4;   //传输出错
    private final byte UP_STA_ERROR = 5;      //传输超时

    //设备返回给APP的指令
    public static final int DEV_MSG_DATA_BEGIN = 0; //数据包起始帧
    public static final int DEV_MSG_DATA_DATA = 1; //数据包数据帧
    public static final int DEV_MSG_DATA_END = 2; //数据包结束帧
    public static final int DEV_MSG_CYQ_BEGIN = 6; //冲牙器起始帧
    public static final int DEV_MSG_CYQ_DATA = 7; //冲牙器数据帧
    public static final int DEV_MSG_CYQ_END = 8; //冲牙器结束帧
    public static final int DEV_MSG_RESULT_BEGIN = 10; //结果包起始帧
    public static final int DEV_MSG_RESULT_DATA = 11; //结果包数据帧
    public static final int DEV_MSG_RESULT_END = 12; //结果包结束帧
    public static final int DEV_MSG_RUNTIME_DATA = 20; //实时数据
    public static final int DEV_MSG_RUNTIME_ACK = 21; //实时应答
    public static final int DEV_MSG_MOTOR_TYPE = (byte) 0x80; //电机参数
    public static final int DEV_MSG_MOTOR_INFO = (byte) 0x83; //电机配置
    public static final int DEV_MSG_INTO = (byte) 0xa0; //设备信息
    public static final int DEV_MSG_ADJUST = (byte) 0xb0; //设备校准
    public static final int DEV_MSG_NO_DATA = (byte) 0xff; //无数据返回

    ///以下这部分数据，可以开放给外面 供显示传输处理用。
    public UpdateStatus status = new UpdateStatus();
    public ToothbrushInfoEntity.Builder tbInfo = ToothbrushInfoEntity.create();   //当前设备信息
    public ToothbrushSettingConfigEntity.Builder settingConfigEntity = ToothbrushSettingConfigEntity.create();   //电机配置范围等
    public ToothbrushSettingEntity.Builder settingEntity = ToothbrushSettingEntity.create();    //当前配置内容
    public String deviceId;
    private int brushTime;
    private OnTransferCallback callback;

    public Transfer(String deviceId, OnTransferCallback callback) {
        this.deviceId = deviceId;
        this.callback = callback;
    }

    //解析从牙刷收到的数据 外部调用， int rtn_num =  xxx.transfer(msg) ,根据 返回值 更新显示……等
    public int transfer(byte[] bytes) {
        if (bytes.length != LEN_FRAME) {    //默认一条数据是20字节， 如果长度<20bytes 可能是其它指令 单独处理
            return -1;
        }
        ByteBuffer data = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        byte type = data.get(); //先获取前4个字节
        byte pagenum = data.get();
        short index = data.getShort();
        int time;
        StringBuilder logBuilder = new StringBuilder();
        switch ((int) type) {
            case DEV_MSG_DATA_BEGIN:    //0    起始帧
            case DEV_MSG_RESULT_BEGIN:
            case DEV_MSG_CYQ_BEGIN:
//                pagesSize++;
                /*以下是解析接收到的数据， 按顺序和长度解析*/
                this.tbInfo.setSoftware(data.getShort());  //版本号 -SOFT
                this.tbInfo.setHardware(data.getShort());  //
                this.tbInfo.setFactory(data.getShort());
                this.tbInfo.setModel(data.getShort());
                this.status.totalFrame = data.getShort();    //总帧数
                this.tbInfo.setPower(data.getShort());          //电量
                this.brushTime = data.getInt();

                /* 更新状态 */
                this.status.runType = UP_STA_START;     //状态， 1-开始传输数据，2-
                this.status.timeout = 0;     //超时计数
                this.status.currentIndex = 0;   //当前帧
                this.status.totalBytes = (this.status.totalFrame + 2) * LEN_FRAME;   //总字节数，加前后两条
                this.status.remainRecordCount = pagenum;   //总条数-倒数
                this.status.result = new byte[status.totalBytes];    //总字节数 分配内容
                this.status.timer = new Timer();  //启动定时器
                this.status.timer.schedule(new DataProcessTimer(), 0, 200);
                this.copy2buffer(0, bytes); //放首条内容
                if (this.callback != null) {
                    this.callback.onReadStart(this.deviceId);
                }
                break;
            case DEV_MSG_DATA_DATA:     //1   数据帧，中间的内容部分
            case DEV_MSG_RESULT_DATA:
            case DEV_MSG_CYQ_DATA:
                status.timeout = status.runType == UP_STA_RESEND ? 2 : 0;  //如果是重传可以快速进入定时判断
                this.copy2buffer(index + 1, bytes);   //按位置放各条数据的内容
                break;
            case DEV_MSG_DATA_END:      //2 数据结束
            case DEV_MSG_RESULT_END:
            case DEV_MSG_CYQ_END:
                this.copy2buffer(this.status.totalFrame + 1, bytes);  //放最后一条的内容
                this.status.timeout = 100;              //下一时刻立即进入重传模式判断
                this.status.runType = UP_STA_RESEND;    //转到重传模式
                break;
            case DEV_MSG_NO_DATA:       //0xff 没有数据
                this.tbInfo.setPower(data.getShort());     //电量
                this.tbInfo.setSoftware(data.getShort());     //版本号 -SOFT
                this.tbInfo.setHardware(data.getShort());     //
                this.tbInfo.setFactory(data.getShort());
                this.tbInfo.setModel(data.getShort());
                if (this.status.usableResult() || this.status.usableResultList()) { //如果有数据
                    this.complete();
                } else {
                    if (this.callback != null) {
                        this.callback.onNoData(this.deviceId);
                    }
                }
                break;
            case DEV_MSG_RUNTIME_DATA:   //实时数据
                RunTimeEntity runTimeEntity = new RunTimeEntity();
                runTimeEntity.rtIndex = index;                   //帧号    由此判断刷牙时间
                runTimeEntity.rtAngle = (pagenum & (1 << 0)) == 1;   //角度是否正确 0正确 1错误
                runTimeEntity.rtRange = (pagenum & (1 << 1)) == 1;   //幅度是否正确 0正确 1错误
                runTimeEntity.rtPressOk = (pagenum & (1 << 2)) == 1; //压力是否正确 0正确 1错误
                for (int i = 0; i < 4; i++) {     //四元数，发给动画
                    runTimeEntity.rt[i] = data.getFloat();
                }
                if (this.callback != null) {
                    this.callback.onRuntime(deviceId, runTimeEntity);
                }
                break;
            case DEV_MSG_RUNTIME_ACK:
                break;
            case DEV_MSG_MOTOR_TYPE:
                this.settingEntity.setMode(index);
                this.settingEntity.setPwm(data.getShort());
                this.settingEntity.setTclk(data.getShort());
                this.settingEntity.setTime(data.getShort());
                this.settingEntity.setStatus(data.getShort());
                if (this.callback != null) {
                    this.callback.onSettingInfo(deviceId, this.settingEntity.build());
                }
                break;
            case DEV_MSG_MOTOR_INFO:
                this.settingConfigEntity.setStandardBash(((pagenum & 0x1) == 1));
                this.settingConfigEntity.setStandardGb(((pagenum >> 4) & 0x08) != 0);
                this.settingConfigEntity.setCanSetting(data.getShort() != 0);
                this.settingConfigEntity.setBasicModelNum(data.getShort());
                this.settingConfigEntity.setMinTClk(data.getShort());
                this.settingConfigEntity.setMaxTClk(data.getShort());
                this.settingConfigEntity.setMaxPwm(data.getShort());
                this.settingConfigEntity.setMinPwn(data.getShort());
                this.settingConfigEntity.setMaxTime(data.getShort());
                this.settingConfigEntity.setMinTime(data.getShort());
                if (this.callback != null) {
                    this.callback.onSettingConfig(deviceId, this.settingConfigEntity.build());
                }
                break;
            case DEV_MSG_INTO:
                break;
            case DEV_MSG_ADJUST:
                break;
        }
        return type;
    }

    // 将buf内容按index的位置 复制到 result里
    private void copy2buffer(int index, byte[] buf) {
        if (this.status.result.length > 0) {   //判断是否给result 分配了内存
            System.arraycopy(buf, 0, this.status.result, index * LEN_FRAME, LEN_FRAME);
        }
    }

    //判断丢失的数据帧，将丢失的帧号码依次放入missIndex中
    private int checkMiss() {
        this.status.missIndex.clear();
        if (this.status.result.length > 0) {
            for (int i = 1; i < this.status.totalFrame - 1; i++) {
                int index = (int) this.status.result[i * LEN_FRAME + 3] + this.status.result[i * LEN_FRAME + 2]; //计算当前的帧号码 第3&2字节
                if (index != i - 1) {
                    this.status.missIndex.add(i - 1);    //否则添加进这一帧的序号，以便重传
                }
            }
        }
        return this.status.missIndex.size();//> 0 ? true : false;
    }

    //传输完成，将数据发送给服务器
    private void complete() {
        //调用外部函数 将数据发送 给服务器 m_info 为当前设备信息， 含id/版本/电量等
        if (this.callback != null) {
            ToothbrushEntity entity = ToothbrushEntity.create()
                    .setContent(this.result2base64())
                    .setDeviceid(this.deviceId)
                    .setTime(this.brushTime)
                    .build();
            this.callback.onComplete(this.deviceId, this.tbInfo.build(), entity);
        }
        this.status.runType = UP_STA_IDLE;     //状态， 1-开始传输数据，2-
        this.status.timeout = 0;     //超时计数
        this.status.totalFrame = 0;  //总帧数
        this.status.currentIndex = 0;   //当前传输的条数 - 弃用
        this.status.totalBytes = 0;
        this.status.remainRecordCount = 0;   //总条数-倒数
        this.status.result = null;   //【还是 = null】
        this.status.resultList.clear(); //数据区域清空， 如果是C/C++ 需要释放内存
        this.status.missIndex.clear();
        if (this.status.timer != null) {
            this.status.timer.cancel();
            this.status.timer = null;
        }
    }

    //将获取到的结果转为 base64 格式 ，
    private String result2base64() {
        int len = this.status.resultList.size();
        int totalBytes = 0;
        for (int i = 0; i < len; i++) {
            //将每一条的长度加起来-总条目的字节数【1 不知道是size还是length 2 或者之前判断总字节数】
            totalBytes += this.status.resultList.get(i).length;
        }
        ByteBuffer data = ByteBuffer.allocate(totalBytes).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < len; i++) {   //按每一条
            for (byte[] item : this.status.resultList) {    //按每一个字节总复制到 data里面
                data.put(item);
            }
        }
        return Bytes.bytes2base64(data.array());
    }

    private void sendBLE(byte[] bytes) {
        if (this.callback == null) {
            return;
        }
        this.callback.onSend2BLE(this.deviceId, bytes);
    }

    /**********************************************************************************************/

    /**
     * 上传数据的状态
     */
    public static class UpdateStatus {
        public Timer timer = null;     //定时器是否启动
        public int runType = -1;     //状态，
        public int timeout = 0;     //超时计数
        public int totalFrame = 0;  //总帧数
        public int currentIndex = -1;//当前帧
        public int totalBytes = 0;  //
        public int remainRecordCount = 0;//总条数 倒数
        public List<Integer> missIndex = new ArrayList<>();
        public List<byte[]> resultList = new ArrayList<>();
        public byte[] result;

        public boolean usableResult() {
            return this.result != null && this.result.length > 0;
        }

        public boolean usableResultList() {
            return this.resultList != null && this.resultList.size() > 0;
        }
    }

    //定时器部分，定时器100ms执行， 根据当前传输的状态 判断超时时间执行相应的流程
    private class DataProcessTimer extends TimerTask {  //1s
        private int resetCount = 0;

        //【这里里面的 this 可能有问题，就是用外层的数据 ，看看怎么改动下 】
        @Override
        public void run() {
            if (status.runType <= UP_STA_IDLE) { //非接收数据过程，什么也不执行，//可以释放timer
                status.timeout = 0;
                this.resetCount = 0;
                if (status.timer != null) {   //如果定时器运行 则停止
                    status.timer.cancel();
                    status.timer = null;
                }
                return;
            } else if (status.runType == UP_STA_COMPLETE) { //传输完，但没接到 nodatas 超时发送数据
                if (status.timeout >= 5) {
                    complete();
                    this.resetCount = 0;
                    if (status.timer != null) {
                        status.timer.cancel();
                        status.timer = null;
                    }
                }
            } else {
                //1 接收数据    2-核对数据 3-一次传完
                if ((status.runType == UP_STA_START && status.timeout >= 5) ||   //接收中
                        (status.runType == UP_STA_RESEND && status.timeout >= 3)) {   //重传
                    status.timeout = 0;
                    int missLen = checkMiss(); //获取丢失的数据帧索引
                    if (missLen == 0 || this.resetCount > 0) {  // 已经完整了， resetCount -- 之前有个bug，这里只重传一次
                        if (this.resetCount > 0) {
                            this.resetCount = 0;
                        }
                        status.resultList.add(status.result); //添加到list
                        status.result = null;    //将此置为空
                        // 通知设备删除已传输的数据 第${status.remainRecordCount}条`);
                        sendBLE(Send2Buffer.complete());    //向蓝牙发送成功指令 等待传下一条
                        this.resetCount = 0;
                        status.runType = UP_STA_COMPLETE;    //当前这条同步完成
                        status.timeout = 0;
                        status.remainRecordCount = 0;
                    } else {    //数据不完整则进入重传
                        status.runType = UP_STA_RESEND;
                        status.timeout = 0;
                        this.resetCount++;
                        sendBLE(Send2Buffer.dataRemap(status.missIndex));
                    }
                }
            }
            status.timeout++;
        }
    }

    public interface OnTransferCallback {
        void onSend2BLE(String deviceId, byte[] bytes);

        void onReadStart(String deviceId);

        void onComplete(String deviceId, ToothbrushInfoEntity tbInfo, ToothbrushEntity tbData);

        void onNoData(String deviceId);

        void onRuntime(String deviceId, RunTimeEntity runTimeEntity);

        void onSettingInfo(String deviceId, ToothbrushSettingEntity settingEntity);

        void onSettingConfig(String deviceId, ToothbrushSettingConfigEntity configEntity);
    }
}
