package com.pandadentist.bleconnection.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

/**
 * CreateTime 2021/6/20 11:48
 * Author zhangwy
 * desc:
 * <p>
 * -------------------------------------------------------------------------------------------------
 * use:
 **/
@SuppressWarnings({"unused"})
@SuppressLint("StaticFieldLeak")
public class Toasts {
    private static Context sContext;

    private Toasts() {
    }

    public static void register(Context context) {
        sContext = context.getApplicationContext();
    }

    private static void check() {
        if (sContext == null) {
            throw new NullPointerException("Must initial call ToastUtils.register(Context context) in your " + "<? " + "extends Application class>");
        }
    }

    public static void showShort(int resId) {
        check();
        Toast.makeText(sContext, resId, Toast.LENGTH_SHORT).show();
    }

    public static void showShort(String message) {
        check();
        Toast.makeText(sContext, message, Toast.LENGTH_SHORT).show();
    }

    public static void showLong(int resId) {
        check();
        Toast.makeText(sContext, resId, Toast.LENGTH_LONG).show();
    }

    public static void showLong(String message) {
        check();
        Toast.makeText(sContext, message, Toast.LENGTH_LONG).show();
    }
}
