package com.pandadentist.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Author: zhangwy(张维亚)
 * 创建时间：2017/3/31 下午5:27
 * 修改时间：2017/3/31 下午5:27
 * Description:
 */

class DatabaseHelperImpl extends DatabaseHelper {

    DatabaseHelperImpl(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (config != null && config.getListener() != null) {
            config.getListener().onCreate(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (config != null && config.getListener() != null) {
            config.getListener().onUpgrade(db, oldVersion, newVersion);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (config != null && config.getListener() != null) {
            config.getListener().onDowngrade(db, oldVersion, newVersion);
        }
    }

    @Override
    public SQLiteDatabase open() throws NullDataBaseException {
        SQLiteDatabase database = this.getWritableDatabase();
        if (database == null)
            throw new NullDataBaseException();
        if (!database.isOpen())
            this.onOpen(database);
        return database;
    }
}
