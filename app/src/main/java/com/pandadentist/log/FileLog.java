package com.pandadentist.log;

import android.text.TextUtils;

import com.pandadentist.util.FileUtil;
import com.pandadentist.util.Logger;
import com.pandadentist.util.TimeUtil;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
public class FileLog {
    /**
     * default  file size limit
     */
    private final static int DEFAULT_MAX_LOG_FILE_SIZE = 1024 * 1024 * 4; // 4M

    /**
     * default log list count limit
     */
    private final static int DEFAULT_MAX_LOG_LIST_COUNT = 2000;

    /**
     * log file name
     */
    private final static String LOG_NAME = "runtime.log";

    /**
     * period of log timer to schedule
     */
    private final static long LOG_TIMER_PERIOD = 1000;

    /**
     * the spliter between log items
     */
    private final static String LOG_SPLITER = ",";

    /**
     * log path
     */
    private static String mLogPath = null;

    /**
     * the size limit of log file
     */
    private static long mLogFileSizeLimit = DEFAULT_MAX_LOG_FILE_SIZE;

    /**
     * the count limit of log list
     */
    private static int mLogListCountLimit = DEFAULT_MAX_LOG_LIST_COUNT;

    /**
     * log list to save log string item
     */
    private static ArrayList<String> mLogList = null;

    /**
     * log list for file write thread
     */
    private static ArrayList<String> mWriteList = null;

    /**
     * true if internal inited, false if otherwise
     */
    private static boolean isInternalInit = false;

    /**
     * file writer to write log
     */
    private static FileWriter mFileWriter = null;

    /**
     * size of log file
     */
    private static long mFileSize = 0;

    /**
     * init file log
     *
     * @param logPath :log path
     */
    public static void init(String logPath) {
        mLogPath = logPath;
        makeDirs(mLogPath);
    }

    /**
     * init file log
     *
     * @param logPath:log       path
     * @param logFileSizeLimit  :size limit of log file,if log file's size exceed this limit, new empty file should be created.
     * @param logListCountLimit :count limit of log list,if exceed this limit, log will be droped.
     */
    public static void init(String logPath, int logFileSizeLimit, int logListCountLimit) {
        mLogPath = logPath;
        mLogFileSizeLimit = logFileSizeLimit;
        mLogListCountLimit = logListCountLimit;
        makeDirs(mLogPath);
    }

    /**
     * log user behavior info
     *
     * @param message:log message
     */
    public synchronized static void b(String message) {
        try {
            if (!isInternalInit) {
                mLogList = new ArrayList<>();
                initTimer();
                isInternalInit = true;
            }

            if (TextUtils.isEmpty(message) || mLogList.size() > mLogListCountLimit) {
                return;
            }

            mLogList.add(TimeUtil.getCurrentDate(TimeUtil.PATTERN_DATE_MS) + "    " + message + "\n");
        } catch (Exception e) {
            Logger.e("b", e);
        }
    }

    /**
     * get log items from @mLogList to @mWriteList,called by timer thread
     */
    @SuppressWarnings("unchecked")
    private synchronized static void getLog() {
        if (mLogList != null) {
            mWriteList = (ArrayList<String>) mLogList.clone();
            mLogList.clear();
        }
    }

    /**
     * write @mWriteList to file,called by timer thread
     */
    private static void writeLog() {
        if (mFileWriter == null) {
            openLogFile(false);
        }

        if (mWriteList == null || mWriteList.isEmpty() || mFileWriter == null) {
            return;
        }

        try {
            for (int i = 0; i < mWriteList.size(); i++) {
                String message = mWriteList.get(i);
                mFileWriter.write(message);
                mFileSize += message.getBytes().length;

                if (mFileSize > mLogFileSizeLimit) {
                    openLogFile(true);
                }
            }
            mFileWriter.flush();
        } catch (Exception e) {
            Logger.e("openLogFile", e);
        }

        mWriteList.clear();
    }

    /**
     * open log  file
     *
     * @param deleteOld: true if delete old file, false otherwise
     */
    private static void openLogFile(boolean deleteOld) {
        try {
            if (TextUtils.isEmpty(mLogPath)) {
                return;
            }

            if (mFileWriter != null) {
                mFileWriter.close();
                mFileWriter = null;
            }

            String logTime = TimeUtil.getCurrentDate(TimeUtil.PATTERN_DATE_MS);
            String realFilePath = FileUtil.pathAddBackslash(mLogPath) + LOG_NAME;

            File logFile = new File(realFilePath);
            if (deleteOld && logFile.exists()) {
                logFile.delete();
            }

            mFileWriter = new FileWriter(logFile, true);
            mFileSize = logFile.length();
        } catch (Exception e) {
            Logger.e("openLogFile", e);
        }
    }

    /**
     * init timer
     */
    private static void initTimer() {
        /* log timer to write log */
        Timer mTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                getLog();
                writeLog();
            }
        };
        mTimer.schedule(timerTask, 0, LOG_TIMER_PERIOD);
    }

    /**
     * check dir and make dir if not exist
     *
     * @param path 路径
     */
    private static void makeDirs(String path) {
        File targetDir = new File(path);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
    }
}
