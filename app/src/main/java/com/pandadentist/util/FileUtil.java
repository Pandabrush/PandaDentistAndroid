package com.pandadentist.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangwy on 2017/9/23.
 * Updated by zhangwy on 2017/9/23.
 * Description
 */
@SuppressWarnings("unused")
public class FileUtil {

    public static boolean makeDirs(String dir) {
        return makeDirs(new File(dir));
    }

    public static boolean makeDirs(File file) {
        return file.mkdirs();
    }

    // 去掉path中的反斜线
    public static String pathRemoveBackslash(String path) {
        if (path == null)
            return path;
        if (path.length() == 0)
            return path;
        //
        char ch = path.charAt(path.length() - 1);
        if (ch == '/' || ch == '\\')
            return path.substring(0, path.length() - 1);
        return path;

    }

    // 在path中添加反斜线
    public static String pathAddBackslash(String path) {
        if (path == null)
            return java.io.File.separator;
        if (path.length() == 0)
            return java.io.File.separator;
        //
        char ch = path.charAt(path.length() - 1);
        if (ch == '/' || ch == '\\')
            return path;
        return path + java.io.File.separator;
    }

    /**
     * clear the files in the directory that match the regex
     *
     * @param regex: regex of the file name
     */
    public static void clear(String dir, String regex) {
        try {
            File[] files = new File(dir).listFiles();
            if (Util.isEmpty(files))
                return;
            for (File file : files) {
                if (regex != null) {
                    if (file.getName().matches(regex)) {
                        file.delete();
                    }
                } else {
                    file.delete();
                }
            }
        } catch (Exception e) {
            Logger.e("clear(String dir, String regex)", e);
        }
    }

    public static void clear(String dir, String regex, long millionSecondsAgo) {
        try {
            File[] files = new File(dir).listFiles();
            if (Util.isEmpty(files))
                return;
            for (File file : files) {
                if (regex != null) {
                    if (file.getName().matches(regex)) {
                        if (System.currentTimeMillis() - file.lastModified() > millionSecondsAgo) {
                            file.delete();
                        }
                    }
                } else {
                    if (System.currentTimeMillis() - file.lastModified() > millionSecondsAgo) {
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            Logger.e("clear(String dir, String regex, long millionSecondsAgo)", e);
        }
    }

    public static List<Elements> readElementsFromAsset(Context context) {
        return readLineFromAsset(context, "013.txt.qq", "    ", new Command<Elements>() {
            @Override
            public Elements execute(String[] array) {
                if (array.length < 5)
                    return null;
                Elements elements = new Elements();
                elements.setTime(array[0]);
                elements.setFirst(array[1]);
                elements.setSecond(array[2]);
                elements.setThird(array[3]);
                elements.setFour(array[4]);
                return elements;
            }
        });
    }

    public static List<CoordInate> readCoordInateFromAsset(Context context) {
        return readLineFromAsset(context, "013.txt.xyz", "    ", new Command<CoordInate>() {
            @Override
            public CoordInate execute(String[] array) {
                if (array.length < 4)
                    return null;
                CoordInate coordInate = new CoordInate();
                coordInate.setTime(array[0]);
                coordInate.setX(array[1]);
                coordInate.setY(array[2]);
                coordInate.setZ(array[3]);
                return coordInate;
            }
        });
    }

    private static <T> List<T> readLineFromAsset(Context context, String fileName, String regex, Command<T> command) {
        ArrayList<T> list = new ArrayList<>();
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while (!TextUtils.isEmpty(line = bufferedReader.readLine())) {
                String[] array = line.split(regex);
                T entity = command.execute(array);
                if (entity == null)
                    continue;
                list.add(entity);
            }
            //close
            inputStream.close();
            bufferedReader.close();
        } catch (Exception e) {
            Log.e("FileUtil", "readLineFromAsset+" + fileName, e);
        }
        return list;
    }

    private interface Command<T> {
        T execute(String[] array);
    }

    public static class Elements {
        private float first = 0.0f;
        private float second = 0.0f;
        private float third = 0.0f;
        private float four = 0.0f;
        private float time = 0.0f;

        public float getTime() {
            return this.time;
        }

        public void setTime(String time) {
            if (!TextUtils.isEmpty(time)) {
                this.time = Float.valueOf(time.replace(" ", ""));
            }
        }

        public float getFirst() {
            return first;
        }

        private void setFirst(String first) {
            if (!TextUtils.isEmpty(first)) {
                this.first = Float.valueOf(first.replace(" ", ""));
            }
        }

        public float getSecond() {
            return second;
        }

        private void setSecond(String second) {
            if (!TextUtils.isEmpty(second)) {
                this.second = Float.valueOf(second.replace(" ", ""));
            }
        }

        public float getThird() {
            return third;
        }

        private void setThird(String third) {
            if (!TextUtils.isEmpty(third)) {
                this.third = Float.valueOf(third.replace(" ", ""));
            }
        }

        public float getFour() {
            return four;
        }

        private void setFour(String four) {
            if (!TextUtils.isEmpty(four)) {
                this.four = Float.valueOf(four.replace(" ", ""));
            }
        }
    }

    public static class CoordInate {
        private float time;
        private float x;
        private float y;
        private float z;

        public float getTime() {
            return time;
        }

        public void setTime(String time) {
            if (!TextUtils.isEmpty(time)) {
                this.time = Float.valueOf(time.replace(" ", ""));
            }
        }

        public float getX() {
            return x;
        }

        private void setX(String x) {
            if (!TextUtils.isEmpty(x)) {
                this.x = Float.valueOf(x.replace(" ", ""));
            }
        }

        public float getY() {
            return y;
        }

        private void setY(String y) {
            if (!TextUtils.isEmpty(y)) {
                this.y = Float.valueOf(y.replace(" ", ""));
            }
        }

        public float getZ() {
            return z;
        }

        private void setZ(String z) {
            if (!TextUtils.isEmpty(z)) {
                this.z = Float.valueOf(z.replace(" ", ""));
            }
        }
    }
}
