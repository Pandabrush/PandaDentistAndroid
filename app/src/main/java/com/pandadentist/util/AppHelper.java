package com.pandadentist.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by Ford on 2016/5/26 0026.
 * <p>
 * app 帮助类
 */
public class AppHelper {

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    public static String getDownloadPerSize(long finished, long total) {
        return DF.format((float) finished / (1024 * 1024)) + "M/" + DF.format((float) total / (1024 * 1024)) + "M";
    }

    public static void installApp(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
}
