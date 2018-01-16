package com.pandadentist.log;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.pandadentist.util.DirMgmt;

import java.util.ArrayList;

/**
 * Created by zhangwy on 2018/1/13.
 * Updated by zhangwy on 2018/1/13.
 * Description
 */
@SuppressWarnings("unused")
public abstract class RunTimeLog {

    private static RunTimeLog instance;

    public static RunTimeLog getInstance(Context context) {
        if (instance == null) {
            synchronized (RunTimeLog.class) {
                if (instance == null) {
                    instance = new RunTimeLogImpl(context);
                }
            }
        }
        return instance;
    }

    public abstract void log(LogAction action, Result result, Object content, long time);

    public abstract void register(OnRunTimeLogListener listener);

    public abstract void unRegister(OnRunTimeLogListener listener);

    private static class RunTimeLogImpl extends RunTimeLog implements Handler.Callback {

        private final int WAIT_LOG_ITEM = 100;
        private Context context;
        private Handler handler = new Handler(Looper.getMainLooper(), this);
        private ArrayList<OnRunTimeLogListener> listeners = new ArrayList<>();

        private RunTimeLogImpl(Context context) {
            this.context = context.getApplicationContext();
            DirMgmt.getInstance().init(this.context);
            FileLog.init(DirMgmt.getInstance().getPath(DirMgmt.WorkDir.LOG));
        }

        @Override
        public void log(LogAction action, Result result, Object content, long time) {
            this.handler.sendMessage(this.handler.obtainMessage(WAIT_LOG_ITEM, new RunTimeLogItem(action, result, content, time)));
            StringBuffer buffer = new StringBuffer();
            buffer.append("action:").append(action.name()).append('_').append(result.name()).append(';');
            buffer.append("content:").append(String.valueOf(content)).append(';');
            buffer.append("usetime:").append(time);
            FileLog.b(buffer.toString());
        }

        @Override
        public void register(OnRunTimeLogListener listener) {
            if (this.listeners.contains(listener))
                return;
            this.listeners.add(listener);
        }

        @Override
        public void unRegister(OnRunTimeLogListener listener) {
            if (listener != null && this.listeners.contains(listener)) {
                this.listeners.remove(listener);
            }
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == WAIT_LOG_ITEM && msg.obj != null && msg.obj instanceof RunTimeLogItem) {
                notifyAll((RunTimeLogItem) msg.obj);
                return true;
            }
            return false;
        }

        private void notifyAll(RunTimeLogItem item) {
            OnRunTimeLogListener[] arrLocal;

            synchronized (this) {
                arrLocal = listeners.toArray(new OnRunTimeLogListener[listeners.size()]);
            }

            for (int i = arrLocal.length-1; i>=0; i--) {
                arrLocal[i].onLog(item);
            }
        }
    }

    public enum LogAction {
        DISCONNECT(0, "disconnect", "断开连接"), CONNECT(0, "connect", "连接"), SCAN(0, "scan", "扫描"), TRANSMIT(0, "transmit", "传输数据"), UPDATE(0, "update", "上传数据");
        public int code;
        public String name;
        public String desc;

        LogAction(int code, String name, String desc) {
            this.code = code;
            this.name = name;
            this.desc = desc;
        }
    }

    public enum Result {
        FAILED(0, "failed", "失败"), SUCCESS(0, "success", "成功"), START(0, "start", "开始"), END(0, "end", "结束"), ERROR(0, "error", "错误"), ANIM(0, "", "");
        public int code;
        public String name;
        public String desc;

        Result(int code, String name, String desc) {
            this.code = code;
            this.name = name;
            this.desc = desc;
        }
    }

    public interface OnRunTimeLogListener {
        void onLog(RunTimeLogItem item);
    }

    public static class RunTimeLogItem {
        public LogAction action;
        public Result result;
        public Object content;
        public long useTime;
        public long nowTime;

        RunTimeLogItem(LogAction action, Result result, Object content, long useTime) {
            this.action = action;
            this.result = result;
            this.content = content;
            this.useTime = useTime;
            this.nowTime = System.currentTimeMillis();
        }
    }
}
