package com.pandadentist.util;

import android.content.Context;
import android.util.TypedValue;

/**
 * @author  Ford
 * */

public class DensityUtil {

    private static Context sContext;

    private DensityUtil() {
    }

    public static void register(Context context) {
        sContext = context.getApplicationContext();
    }

    private static void check() {
        if (sContext == null) {
            throw new NullPointerException("Must initial call DensityUtil.register(Context context) in your " + "<? " + "extends Application class>");
        }
    }

    public static float dp(int dip){
        check();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, sContext.getResources().getDisplayMetrics());
    }

}