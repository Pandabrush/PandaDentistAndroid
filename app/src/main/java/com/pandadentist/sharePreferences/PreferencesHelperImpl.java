package com.pandadentist.sharePreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Pair;

import com.pandadentist.bleconnection.utils.Util;

import java.util.List;

/**
 * Author: zhangwy(张维亚)
 * 创建时间：2017/4/1 下午3:32
 * 修改时间：2017/4/1 下午3:32
 * Description: SharePreferences封装实现
 */
class PreferencesHelperImpl extends PreferencesHelper {
    private String name;
    private SharedPreferences preferences;

    PreferencesHelperImpl(String name) {
        this.name = name;
    }

    @Override
    public void init(Context ctx) {
        if (preferences == null) {
            synchronized (PreferencesHelperImpl.class) {
                if (preferences == null) {
                    preferences = ctx.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
                }
            }
        }
    }

    @Override
    public boolean commitString(String key, String value) {
        return preferences.edit().putString(key, value).commit();
    }

    @Override
    public void applyString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    @Override
    public boolean commitStrings(List<Pair<String, String>> list) {
        if (Util.isEmpty(list))
            return false;
        Editor editor = preferences.edit();
        for (Pair<String, String> pair : list) {
            editor.putString(pair.first, pair.second);
        }
        return editor.commit();
    }

    @Override
    public void applyStrings(List<Pair<String, String>> list) {
        if (Util.isEmpty(list))
            return;
        Editor editor = preferences.edit();
        for (Pair<String, String> pair : list) {
            editor.putString(pair.first, pair.second);
        }
        editor.apply();
    }

    @Override
    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    @Override
    public boolean commitInt(String key, int value) {
        return preferences.edit().putInt(key, value).commit();
    }

    @Override
    public void applyInt(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    @Override
    public boolean commitInts(List<Pair<String, Integer>> list) {
        if (Util.isEmpty(list))
            return false;
        Editor editor = preferences.edit();
        for (Pair<String, Integer> pair : list) {
            editor.putInt(pair.first, pair.second);
        }
        return editor.commit();
    }

    @Override
    public void applyInts(List<Pair<String, Integer>> list) {
        if (Util.isEmpty(list))
            return;
        Editor editor = preferences.edit();
        for (Pair<String, Integer> pair : list) {
            editor.putInt(pair.first, pair.second);
        }
        editor.apply();
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    @Override
    public boolean commitLong(String key, long value) {
        return preferences.edit().putLong(key, value).commit();
    }

    @Override
    public void applyLong(String key, long value) {
        preferences.edit().putLong(key, value).apply();
    }

    @Override
    public boolean commitLongs(List<Pair<String, Long>> list) {
        if (Util.isEmpty(list))
            return false;
        Editor editor = preferences.edit();
        for (Pair<String, Long> pair : list) {
            editor.putLong(pair.first, pair.second);
        }
        return editor.commit();
    }

    @Override
    public void applyLongs(List<Pair<String, Long>> list) {
        if (Util.isEmpty(list))
            return;
        Editor editor = preferences.edit();
        for (Pair<String, Long> pair : list) {
            editor.putLong(pair.first, pair.second);
        }
        editor.apply();
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }

    @Override
    public boolean commitFloat(String key, float value) {
        return preferences.edit().putFloat(key, value).commit();
    }

    @Override
    public void applyFloat(String key, float value) {
        preferences.edit().putFloat(key, value).apply();
    }

    @Override
    public boolean commitFloats(List<Pair<String, Float>> list) {
        if (Util.isEmpty(list))
            return false;
        Editor editor = preferences.edit();
        for (Pair<String, Float> pair : list) {
            editor.putFloat(pair.first, pair.second);
        }
        return editor.commit();
    }

    @Override
    public void applyFloats(List<Pair<String, Float>> list) {
        if (Util.isEmpty(list))
            return;
        Editor editor = preferences.edit();
        for (Pair<String, Float> pair : list) {
            editor.putFloat(pair.first, pair.second);
        }
        editor.apply();
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return preferences.getFloat(key, defaultValue);
    }

    @Override
    public boolean commitBoolean(String key, boolean value) {
        return preferences.edit().putBoolean(key, value).commit();
    }

    @Override
    public void applyBoolean(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    @Override
    public boolean commitBooleans(List<Pair<String, Boolean>> list) {
        if (Util.isEmpty(list))
            return false;
        Editor editor = preferences.edit();
        for (Pair<String, Boolean> pair : list) {
            editor.putBoolean(pair.first, pair.second);
        }
        return editor.commit();
    }

    @Override
    public void applyBooleans(List<Pair<String, Boolean>> list) {
        if (Util.isEmpty(list))
            return;
        Editor editor = preferences.edit();
        for (Pair<String, Boolean> pair : list) {
            editor.putBoolean(pair.first, pair.second);
        }
        editor.apply();
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    @Override
    public boolean remove(String key) {
        return preferences.edit().remove(key).commit();
    }

    @Override
    public boolean clear() {
        return preferences.edit().clear().commit();
    }
}
