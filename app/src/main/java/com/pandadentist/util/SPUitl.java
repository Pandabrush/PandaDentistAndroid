package com.pandadentist.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.pandadentist.App;
import com.pandadentist.entity.WXEntity;

/**
 * SharedPreferences工具类
 * Created by ld on 16/1/18.
 */
@SuppressWarnings("unused")
public class SPUitl {
    private static String CONFIG = "config";
    private static SharedPreferences sp;

    /**
     * 保存boolean类型的数据到config文件中
     */
    public static void saveBooleanData(Context context, String key, boolean value) {
        if (sp == null) {
            sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        sp.edit().putBoolean(key, value).apply();
    }

    /**
     * 在config文件中获取boolean类型的数据
     */
    public static boolean getBooleanData(Context context, String key, boolean defaultValue) {
        if (sp == null) {
            sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        return sp.getBoolean(key, defaultValue);
    }

    /**
     * 保存String类型的数据到config文件中
     */
    public static void saveStringData(Context context, String key, String value) {
        if (sp == null) {
            sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        sp.edit().putString(key, value).apply();
    }

    /**
     * 在config文件中获取String类型的数据
     */
    public static String getStringData(Context context, String key, String defaultValue) {
        if (sp == null) {
            sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        return sp.getString(key, defaultValue);
    }

    /**
     * 保存Int类型的数据到config文件中
     */
    public static void saveIntData(Context context, String key, int value) {
        if (sp == null) {
            sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        sp.edit().putInt(key, value).apply();
    }

    /**
     * 在config文件中获取String类型的数据
     */
    public static int getIntData(Context context, String key, int defaultValue) {
        if (sp == null) {
            sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        return sp.getInt(key, defaultValue);
    }

    public static void saveSettingVibrate(boolean b) {
        SharedPreferences s = App.sContext.getSharedPreferences("setting", Context.MODE_PRIVATE);
        s.edit().putBoolean("Vibrate", b).apply();
    }

    public static boolean getSettingVibrate() {
        return App.sContext.getSharedPreferences("setting", Context.MODE_PRIVATE).getBoolean("Vibrate", false);
    }

    public static void saveSettingAlarm(boolean b) {
        SharedPreferences s = App.sContext.getSharedPreferences("setting", Context.MODE_PRIVATE);
        s.edit().putBoolean("Alarm", b).apply();
    }

    public static boolean getSettingAlarm() {
        return App.sContext.getSharedPreferences("setting", Context.MODE_PRIVATE).getBoolean("Alarm", false);
    }

    public static void saveSettingNotify(boolean b) {
        SharedPreferences s = App.sContext.getSharedPreferences("setting", Context.MODE_PRIVATE);
        s.edit().putBoolean("Notify", b).apply();
    }

    public static boolean getSettingNotify() {
        return App.sContext.getSharedPreferences("setting", Context.MODE_PRIVATE).getBoolean("Notify", true);
    }

    public static void saveUser(String jsonStr) {
        SharedPreferences s = App.sContext.getSharedPreferences("user", Context.MODE_PRIVATE);
        s.edit().putString("userStr", jsonStr).apply();
    }

    public static void saveChannelId(String channelId) {
        SharedPreferences s = App.sContext.getSharedPreferences("channelId", Context.MODE_PRIVATE);
        s.edit().putString("channelId", channelId).apply();
    }

    public static String getChannelId() {
        return App.sContext.getSharedPreferences("channelId", Context.MODE_PRIVATE).getString("channelId", "");
    }

    public static void clear() {
        SharedPreferences s = App.sContext.getSharedPreferences("user", Context.MODE_PRIVATE);
        s.edit().clear().apply();
        clearToken();
        clearWXUser();
    }

    public static void saveToken(String token) {
        SharedPreferences s = App.sContext.getSharedPreferences("token", Context.MODE_PRIVATE);
        s.edit().putString("token", token).apply();
    }

    public static void saveWXUser(WXEntity wxEntity) {
        String str = new Gson().toJson(wxEntity);
        SharedPreferences s = App.sContext.getSharedPreferences("wxEntity", Context.MODE_PRIVATE);
        s.edit().putString("wxEntity", str).apply();
    }

    public static WXEntity getWXUser() {
        String str = App.sContext.getSharedPreferences("wxEntity", Context.MODE_PRIVATE).getString("wxEntity", "");
        return new Gson().fromJson(str, WXEntity.class);
    }

    public static void clearWXUser() {
        SharedPreferences s = App.sContext.getSharedPreferences("wxEntity", Context.MODE_PRIVATE);
        s.edit().clear().apply();
    }

    public static String getToken() {
        return App.sContext.getSharedPreferences("token", Context.MODE_PRIVATE).getString("token", "");
    }

    public static void clearToken() {
        SharedPreferences s = App.sContext.getSharedPreferences("token", Context.MODE_PRIVATE);
        s.edit().clear().apply();
    }

    public static void saveWiFiPwd(String ssid, String pwd) {
        SharedPreferences s = App.sContext.getSharedPreferences(ssid, Context.MODE_PRIVATE);
        s.edit().putString(ssid, pwd).apply();
    }

    public static void clearWiFi(String ssid) {
        SharedPreferences s = App.sContext.getSharedPreferences(ssid, Context.MODE_PRIVATE);
        s.edit().clear().apply();
    }

    public static String getWiFiPwd(String ssid) {
        return App.sContext.getSharedPreferences(ssid, Context.MODE_PRIVATE).getString(ssid, "");
    }

    public static boolean isLogin() {
        return !TextUtils.isEmpty(SPUitl.getToken());
    }

    public static boolean isFirstRun() {
        return App.sContext.getSharedPreferences("firstrun", Context.MODE_PRIVATE).getBoolean("firstrun", true);
    }

    public static void saveFirsRun(boolean b) {
        SharedPreferences s = App.sContext.getSharedPreferences("firstrun", Context.MODE_PRIVATE);
        s.edit().putBoolean("firstrun", b).apply();
    }

    public static boolean firstLaunch(Context context) {
        if (sp == null) {
            sp = context.getApplicationContext().getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        return Device.App.getVersionCode(context) > sp.getInt("app_launched_version_code", -1);
    }

    public static void launchedApp(Context context) {
        if (sp == null) {
            sp = context.getApplicationContext().getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        sp.edit().putInt("app_launched_version_code", Device.App.getVersionCode(context));
    }
}
