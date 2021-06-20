package com.pandadentist.sharePreferences;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;

import java.util.HashMap;
import java.util.List;

/**
 * Author: zhangwy(张维亚)
 * 创建时间：2017/4/1 下午3:31
 * 修改时间：2017/4/1 下午3:31
 * Description: sharePreferences包装
 * Commit与Apply区别
 * 1. apply没有返回值而commit返回boolean表明修改是否提交成功。
 * 2. apply是将修改数据原子提交到内存, 而后异步真正提交到硬件磁盘, 而commit是同步的提交到硬件磁盘。
 * 3. apply方法不会提示任何失败的提示, 如果对提交的结果不关心的话，建议使用apply，需要确保提交成功且有后续操作的话，还是需要用commit的。
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class PreferencesHelper {

    private static final String DEFAULT_NAME = "zhangwy_self_preferences";
    private static HashMap<String, PreferencesHelper> helpers = new HashMap<>();

    public static PreferencesHelper newInstance(String name) {
        if (TextUtils.isEmpty(name))
            name = DEFAULT_NAME;
        if (!helpers.containsKey(name)) {
            synchronized (PreferencesHelper.class) {
                if (!helpers.containsKey(name)) {
                    helpers.put(name, new PreferencesHelperImpl(name));
                }
            }
        }
        return helpers.get(name);
    }

    public static PreferencesHelper defaultInstance() {
        return newInstance(DEFAULT_NAME);
    }

    public abstract void init(Context ctx);

    public abstract boolean commitString(String key, String value);
    public abstract void applyString(String key, String value);

    public abstract boolean commitStrings(List<Pair<String, String>> list);
    public abstract void applyStrings(List<Pair<String, String>> list);

    public abstract String getString(String key, String defaultValue);

    public abstract boolean commitInt(String key, int value);
    public abstract void applyInt(String key, int value);

    public abstract boolean commitInts(List<Pair<String, Integer>> list);
    public abstract void applyInts(List<Pair<String, Integer>> list);

    public abstract int getInt(String key, int defaultValue);

    public abstract boolean commitLong(String key, long value);
    public abstract void applyLong(String key, long value);

    public abstract boolean commitLongs(List<Pair<String, Long>> list);
    public abstract void applyLongs(List<Pair<String, Long>> list);

    public abstract long getLong(String key, long defaultValue);

    public abstract boolean commitFloat(String key, float value);
    public abstract void applyFloat(String key, float value);

    public abstract boolean commitFloats(List<Pair<String, Float>> list);
    public abstract void applyFloats(List<Pair<String, Float>> list);

    public abstract float getFloat(String key, float defaultValue);

    public abstract boolean commitBoolean(String key, boolean value);
    public abstract void applyBoolean(String key, boolean value);

    public abstract boolean commitBooleans(List<Pair<String, Boolean>> list);
    public abstract void applyBooleans(List<Pair<String, Boolean>> list);

    public abstract boolean getBoolean(String key, boolean defaultValue);

    public abstract boolean remove(String key);

    public abstract boolean clear();
}