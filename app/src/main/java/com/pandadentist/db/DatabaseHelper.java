package com.pandadentist.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * Author: zhangwy(张维亚)
 * 创建时间：2017/3/31 下午3:08
 * 修改时间：2017/3/31 下午3:08
 * Description:database帮助类，继承自SQLiteOpenHelper
 * 该类会根据给定信息创建Or打开相应数据库
 */

public abstract class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DEFAULT_DATABASE = "database";

    private static final HashMap<DatabaseConfig, DatabaseHelper> helpers = new HashMap<>();

    public static DatabaseHelper create(Context ctx, DatabaseConfig config) {
        if (config == null) {
            config = DatabaseConfig.defaultInstance();
        }
        DatabaseHelper helper = helpers.get(config);
        if (helper == null) {
            synchronized (DatabaseHelper.class) {
                helper = helpers.get(config);
                if (helper == null) {
                    helper = new DatabaseHelperImpl(ctx.getApplicationContext(), config.name, null, config.version);
                    helper.config = config;
                    helpers.put(config, helper);
                }
            }
        }
        return helper;
    }

    DatabaseConfig config;

    protected DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public abstract SQLiteDatabase open() throws NullDataBaseException;

    /************************************************************************************************************************************************/
    public interface UpgradeListener {
        void onCreate(SQLiteDatabase database);
        void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion);
        void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion);
    }

    public static class DatabaseConfig {
        private String name = DEFAULT_DATABASE;
        private int version = 1;
        private UpgradeListener listener;

        private DatabaseConfig() {
        }

        private static DatabaseConfig defaultConfig;

        public static DatabaseConfig defaultInstance() {
            if (defaultConfig == null) {
                synchronized (DatabaseHelper.class) {
                    if (defaultConfig == null) {
                        defaultConfig = new DatabaseConfig();
                    }
                }
            }
            return defaultConfig;
        }

        public static DatabaseConfig newInstance(String name, int version, UpgradeListener listener) {
            DatabaseConfig config = new DatabaseConfig();
            config.name = TextUtils.isEmpty(name) ? DEFAULT_DATABASE : name;
            config.version = version;
            config.listener = listener;
            return config;
        }

        public UpgradeListener getListener() {
            return this.listener;
        }
    }

    public static class NullDataBaseException extends Exception {
        private static final long serialVersionUID = 5810176814759834423L;

        public NullDataBaseException(){
            super("database is null");
        }
    }
}
