package com.pandadentist.log;

import android.content.Context;
import android.content.Intent;

import com.pandadentist.util.DirMgmt;
import com.pandadentist.util.Logger;

/**
 * Created by zhangwy on 2018/1/13.
 * Updated by zhangwy on 2018/1/13.
 * Description
 */
@SuppressWarnings("unused")
public abstract class RunTimeLog {
    public final static String RECEIVE_LOG = "com.bestfudaye.PRIVATE.RECEIVE.LOG";
    public final static String EXTRA_ACTION = "com.bestfudaye.PRIVATE.EXTRA.ACTION";
    public final static String EXTRA_ACTION2 = "com.bestfudaye.PRIVATE.EXTRA.ACTION2";
    public final static String EXTRA_DATA = "com.bestfudaye.PRIVATE.EXTRA.DATA.CONTENT";
    public final static String EXTRA_TIME = "com.bestfudaye.PRIVATE.EXTRA.DATA.TIME";

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

    public abstract void log(LogAction action, LogAction2 action2, Object content, long time);

    static class RunTimeLogImpl extends RunTimeLog {

        private final String PERMISSION_RECEIVE_LOG = "com.android.permission.RECEIVE_LOG";

        private Context context;

        private RunTimeLogImpl(Context context) {
            this.context = context.getApplicationContext();
            DirMgmt.getInstance().init(this.context);
            FileLog.init(DirMgmt.getInstance().getPath(DirMgmt.WorkDir.LOG));
        }

        @Override
        public void log(LogAction action, LogAction2 action2, Object content, long time) {
            this.sendBroadcast(action, action2, content, time);
            StringBuffer buffer = new StringBuffer();
            buffer.append("action:").append(action.name()).append('_').append(action2.name()).append(';');
            buffer.append("content:").append(String.valueOf(content)).append(';');
            buffer.append("usetime:").append(time);
            FileLog.b(buffer.toString());
        }

        private void sendBroadcast(LogAction action, LogAction2 action2, Object content, long time) {
            try {
                Intent intent = new Intent(RECEIVE_LOG);
                intent.putExtra(EXTRA_ACTION, action.name());
                intent.putExtra(EXTRA_ACTION2, action2.name());
                intent.putExtra(EXTRA_DATA, String.valueOf(content));
                intent.putExtra(EXTRA_TIME, String.valueOf(time));
                this.context.sendBroadcast(intent, PERMISSION_RECEIVE_LOG);
            } catch (Exception e) {
                Logger.e("sendBroadcast", e);
            }
        }
    }

    public enum LogAction {
        DISCONNECT, CONNECT, SCAN, TRANSMIT,
    }

    public enum LogAction2 {
        ERROR, SUCCESS, START,END,
    }
}
