package com.pandadentist.util;

import android.content.Context;
import android.os.Environment;

import com.pandadentist.task.Task;

/**
 * Author: zhangwy(张维亚)
 * 创建时间：2017/4/7 下午2:27
 * 修改时间：2017/4/7 下午2:27
 * Description: 目录相关
 */
@SuppressWarnings("unused")
public class DirMgmt {

    private final String ROOT = "";
    private static DirMgmt instance;

    public static DirMgmt getInstance() {
        if (instance == null) {
            synchronized (DirMgmt.class) {
                if (instance == null) {
                    instance = new DirMgmt();
                }
            }
        }
        return instance;
    }

    private Context context;
    public void init(Context ctx) {
        this.context = ctx.getApplicationContext();
        this.clearDirs();
        this.makeDirs();
    }

    public String getPath(WorkDir workDir) {
        if (workDir != null) {
            String path = workDir.ram ? getRamRootPath(context) : getRootPath();
            if (!FileUtil.makeDirs(path)) {
                Logger.e("create directory: " + path + " failed!");
            }
            path = FileUtil.pathAddBackslash(path) + workDir.path;
            if (!FileUtil.makeDirs(path)) {
                Logger.e("create directory: " + path + " failed!");
            }
            return path;
        }

        return getRootPath();
    }

    private String getPath(WorkDir workDir, boolean ram) {
        if (ram) {
            String path = getRamRootPath(context);
            if (!FileUtil.makeDirs(path)) {
                Logger.e("create directory: " + path + " failed!");
            }
            path = FileUtil.pathAddBackslash(path) + workDir.path;
            if (!FileUtil.makeDirs(path)) {
                Logger.e("create directory: " + path + " failed!");
            }
            return path;
        } else {
            return getPath(workDir);
        }
    }

    private String getRootPath() {
        String rootPath;
        if (isSdCardAvailable()) {
            rootPath = getSdCardRootPath();
        } else {
            rootPath = getRamRootPath(context);
        }
        if (!FileUtil.makeDirs(rootPath)) {
            Logger.e("create directory: " + rootPath + " failed!");
        }
        return rootPath;
    }

    private String getSdCardRootPath() {
        return FileUtil.pathAddBackslash(sdCardRootPath()) + ROOT;
    }

    private String getRamRootPath(Context ctx) {
        return FileUtil.pathAddBackslash(ramRootPath(ctx)) + ROOT;
    }

    // sd卡路径
    private String sdCardRootPath() {
        try {
            return FileUtil.pathRemoveBackslash(context.getExternalFilesDir(null).getPath());
        } catch (Exception e) {
            return ramRootPath(context);
        }
    }

    // 私有数据目录
    private String ramRootPath(Context ctx) {
        return FileUtil.pathRemoveBackslash(ctx.getFilesDir().getAbsolutePath());
    }

    private Boolean isSdCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private boolean makeDirs() {
        for (WorkDir dir : WorkDir.values()) {
            FileUtil.makeDirs(getPath(dir));
        }
        return true;
    }

    private void clearDirs() {
        Task<DirMgmt> task = new Task<DirMgmt>() {
            @Override
            protected void doInBackground(DirMgmt... params) {
                if (Util.isEmpty(params)) {
                    return;
                }
                DirMgmt mgmt = params[0];
                for (WorkDir dir : WorkDir.values()) {
                    if (dir.useTime > 0) {
                        FileUtil.clear(mgmt.getPath(dir), "", dir.useTime);
                        FileUtil.clear(mgmt.getPath(dir, true), "", dir.useTime);
                    }
                }
            }
        };
        task.execute(this);
    }

    public enum WorkDir {

        IMAGE("image", "广告图片素材目录", false, 7),
        APK("apk", "广告点击下载apk目录", false, -1),
        LOG("logs", "存储log目录", false, -1),
        OTHER("other", "未知文件目录目录", false, -1),;
        public String path;//目录路径
        public String desc;//目录描述
        public boolean ram;//强制使用ram目录
        public long useTime;//有效时间，单位毫秒

        WorkDir(String path, String desc, boolean ram, int useTime) {
            this.path = path;
            this.desc = desc;
            this.ram = ram;
            this.useTime = useTime * 86400000;//86400000=24*60*60*1000一天时间
        }
    }
}
