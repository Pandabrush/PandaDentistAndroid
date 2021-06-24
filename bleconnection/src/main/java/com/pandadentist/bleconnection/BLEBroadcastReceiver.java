package com.pandadentist.bleconnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;

import com.pandadentist.bleconnection.entity.ToothbrushEntity;
import com.pandadentist.bleconnection.service.BLEService;
import com.pandadentist.bleconnection.utils.BLEProtoProcess;
import com.pandadentist.bleconnection.utils.Logger;

import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * CreateTime 2021/6/24 23:09
 * Author zhangwy
 * desc:
 * <p>
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
@SuppressWarnings({"SameParameterValue", "unused"})
public class BLEBroadcastReceiver extends BroadcastReceiver {

    private boolean destroy = false;
    private OnReceiverCallback callback;
    private Handler handler = new Handler(Looper.getMainLooper());
    private HashMap<String, BLEProtoProcess> processMap = new HashMap<>();
    private Timer timer = null;
    private int timecount = 0;
    private int runtype = 0;//0-未运行， 1-接收数据过程， 2-核对丢失帧过程
    private int checkCount = 0;

    public BLEBroadcastReceiver(OnReceiverCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (this.checkDestroy(context) || intent == null || this.callback == null) {
            return;
        }

        final String action = intent.getAction();
        final String address = intent.getStringExtra(BLEService.EXTRA_ADDRESS);
        if (action == null || action.length() == 0
                || address == null || address.length() == 0) {
            return;
        }

        switch (action) {
            case BLEService.DEVICE_DOES_NOT_SUPPORT_UART: {
                Logger.d("Device doesn't support UART. Disconnecting");
                callback.onBleNonSupport(address);
                break;
            }
            case BLEService.ACTION_GATT_CONNECTED: {
                Logger.d("writeRXCharacteristic.writeRXCharacteristic");
                this.postDelayedOnMainThread(() -> this.sync(address), 1000);
                break;
            }

            case BLEService.ACTION_GATT_DISCONNECTED: {
                postOnMainThread(() -> callback.onDisconnected(address));
                break;
            }

            case BLEService.ACTION_GATT_SERVICES_DISCOVERED: {
                callback.onServicesDiscovered(address);
                break;
            }

            case BLEService.ACTION_DATA_AVAILABLE: {
                timecount = 0;
                checkCount = 0;
                final byte[] txValue = intent.getByteArrayExtra(BLEService.EXTRA_DATA);
                BLEProtoProcess process = process(address);
                int status = process.interp(txValue);
                switch (status) {
                    case BLEProtoProcess.BLE_DATA_START:
                    case BLEProtoProcess.BLE_RESULT_START:
                        Logger.d("BLE_DATA_START  and  BLE_RESULT_START");
                        this.callback.onReadStart(address);
                        process.setHasrecieved(true);
                        runtype = 1;
                        timer = new Timer();
                        timer.schedule(new DataProcessTimer(address), 0, 200);
                        break;
                    case BLEProtoProcess.BLE_DATA_RECEIVER:
                        break;
                    case BLEProtoProcess.BLE_DATA_END:
                    case BLEProtoProcess.BLE_RESULT_END:
                        Logger.d("BLE_DATA_END  and  BLE_RESULT_END");
                        runtype = 2;
                        timecount = 100;
                        break;
                    case BLEProtoProcess.BLE_MISSED_RECEIVER:
                        Logger.d("BLE_MISSED_RECEIVER");
                        break;
                    case BLEProtoProcess.BLE_MISSED_END:
                        Logger.d("丢失帧接受完毕");
                        timecount = 100;
                        break;
                    case BLEProtoProcess.BLE_NO_SYNC://没有同步数据
                        if (process.isHasrecieved()) {
                            Logger.d("请求动画");
                            process.setIsreqenddatas(true);
                            this.write(address, process.getRequests((byte) 0, (byte) 1));
                        } else {
                            Logger.d("没有数据同步");
                            this.callback.onNoData(address);
                        }
                        break;
                }
                break;
            }
        }
    }

    private boolean checkDestroy(Context context) {
        if (!this.isDestroy()) {
            return false;
        }
        try {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        } catch (Exception e) {
            Logger.d("unregisterReceiver", e);
        }
        return true;
    }

    public void sync(String address) {
        BLEProtoProcess process = process(address);
        this.write(address, process.getRequests((byte) 1, (byte) 0));
        process.setIsreqenddatas(false);
        process.setHasrecieved(false);
    }

    public void destroy() {
        this.handler.removeCallbacksAndMessages(null);
        this.handler = null;
        this.destroy = true;
        this.callback = null;
    }

    private boolean isDestroy() {
        return this.destroy;
    }

    private boolean checkData(String address) {
        checkCount++;
        try {
            BLEProtoProcess process = process(address);
            if (process.checkMissed() && this.checkCount <= 5) {
                Logger.d("丢帧");
                this.write(address, process.getMissedRequests());
                return false;
            } else {
                //1.发送请求成功帧  2.把数据交给后台处理
                Logger.d("数据接收完毕!");
                this.write(address, process.getCompleted());
                //------------发送数据到服务器
                if (process(address).isreqenddatas()) {
                    if (this.callback != null) {
                        process.setHasrecieved(false);
                        process.setIsreqenddatas(false);
                        ToothbrushEntity entity = ToothbrushEntity.create()
                                .setDeviceid(address)
                                .setSoftware(process.getSoftware())
                                .setFactory(process.getFactory())
                                .setModel(process.getModel())
                                .setPower(process.getPower())
                                .setTime(process.getTime())
                                .setHardware(process.getHardware())
                                .setContent(process.getBuffer())
                                .setDataTyp(process.getDatatype())
                                .setPageSize(process.getPagesSize()).build();
                        this.callback.onRead(address, entity);
                        process.setPageSize(0);
                    }
                }
                return true;
            }
        } catch (Exception e) {
            Logger.e("checkData", e);
        }
        return true;
    }

    private BLEProtoProcess process(String address) {
        BLEProtoProcess process = this.processMap.get(address);
        if (process == null) {
            process = new BLEProtoProcess();
            this.processMap.put(address, process);
        }
        return process;
    }

    private void write(String address, byte[] value) {
        if (this.callback == null) {
            return;
        }
        this.callback.onWrite(address, value);
    }

    private void postDelayedOnMainThread(Runnable action, long delayMillis) {
        if (this.handler != null && action != null) {
            this.handler.postDelayed(action, delayMillis);
        }
    }

    private void postOnMainThread(Runnable action) {
        if (this.handler != null && action != null) {
            this.handler.post(action);
        }
    }

    public interface OnReceiverCallback {

        void onDisconnected(String address);

        void onServicesDiscovered(String address);

        void onBleNonSupport(String address);

        void onReadStart(String address);

        void onRead(String address, ToothbrushEntity entity);

        void onWrite(String address, byte[] bytes);

        void onNoData(String address);
    }

    private class DataProcessTimer extends TimerTask {

        private final String address;

        private DataProcessTimer(String address) {
            this.address = address;
        }

        @Override
        public void run() {
            Logger.d(String.format(Locale.getDefault(), "计时器开始执行count:%1$d;runtype:%2$d", timecount, runtype));
            if (runtype == 0) {//非接收数据过程，什么也不执行，//可以释放timer
                timecount = 0;
                if (timer != null) {
                    timer.cancel();
                }
            } else {
                //1 接收数据    2-核对数据
                if ((runtype == 1 && timecount >= 10) || (runtype == 2 && timecount >= 4)) {
                    timecount = 0;
                    if (checkData(this.address)) {
                        runtype = 0;
                        if (timer != null) {
                            timer.cancel();
                        }
                    }
                }
                timecount++;
            }
        }
    }
}
