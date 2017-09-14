package utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

import application.BaseApplication;
import common.AppConst;

/**
 * Created by yuxh3 on 2017/9/14.
 */

public class SPData {
    private static BaseApplication application = BaseApplication.getInstance();

    private static SharedPreferences getSP() {
        return application.getSharedPreferences(AppConst.SP_NAME, Activity.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getEditer() {
        SharedPreferences sp = getSP();
        return sp.edit();
    }

    public static int getInt(String key) {
        SharedPreferences sharedPreferences = getSP();
        return sharedPreferences.getInt(key, -2);
    }

    public static void saveInt(String key, int b) {
        SharedPreferences.Editor editor = getEditer();
        editor.putInt(key, b);
        editor.commit();
    }

    private static long getLong(String key, int defaultVal) {
        return getSP().getLong(key, defaultVal);
    }

    private static void saveLong(String key, long value) {
        SharedPreferences.Editor editor = getEditer();
        editor.putLong(key, value);
        editor.commit();
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = getSP();
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public static void saveBoolean(String key, boolean b) {
        SharedPreferences.Editor editor = getEditer();
        editor.putBoolean(key, b);
        editor.commit();
    }

    public static String getString(String key) {
        SharedPreferences sharedPreferences = getSP();
        return sharedPreferences.getString(key, "");
    }

    public static void saveString(String key, String value) {
        SharedPreferences.Editor editor = getEditer();
        editor.putString(key, value);
        editor.commit();
    }

    public static void removeString(String key) {
        if (getSP().contains(key)) {
            getEditer().remove(key);
        }
    }

    public static void saveForeverObject(String key, Object object) {
        SharedPreferences.Editor editor = getEditer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            String strList = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
            editor.putString(key, strList);
            editor.commit();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Object getForeverObject(String key) {
        Object result = null;
        SharedPreferences sharedPreferences = getSP();
        String message = sharedPreferences.getString(key, "");
        if (message.equals(""))
            return null;
        byte[] buffer = Base64.decode(message.getBytes(), Base64.DEFAULT);
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            result = ois.readObject();
            ois.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bais.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 存在超时机制的数据
     *
     * @param datalistToStr
     * @param type
     */

    public static void putTimeoutData(String datalistToStr, String type) {
        Object obj = getForeverObject(AppConst.APP_SITE_WEBSITESYSNO);
        if (StringTool.isNull(obj)) {
            return;
        }
        String siteName = obj.toString();
        String dataKey = getTimeoutKey(type, siteName);
        saveString(dataKey, datalistToStr);
        markCurrentTimeWithKey(dataKey);
    }

    /**
     * 获取可超时数据
     *
     * @param type
     * @return
     */
    public static String getTimeoutData(String type) {
        String data = "";
        Object obj = getForeverObject(AppConst.APP_SITE_WEBSITESYSNO);
        if (StringTool.isNull(obj)) {
            return data;
        }
        String siteName = obj.toString();
        String dataKey = getTimeoutKey(type, siteName);

        if (!isKeyTimeout(dataKey)) {
            data = getString(dataKey);
        }

        return data;
    }

    public static void removeTimeoutData(String key) {
        if (getSP().contains(key)) {
            getEditer().remove(key).commit();
        }

        if (getSP().contains("Time:" + key)) {
            getEditer().remove("Time:" + key).commit();
        }
    }

    /**
     * 存在超时的key
     *
     * @param cacheName
     * @param siteName
     * @return
     */
    private static String getTimeoutKey(String cacheName, String siteName) {
        return AppConst.KEY_CACHE_PREFIX + cacheName + siteName;
    }

    public static void markCurrentTimeWithKey(String key) {
        long curTime = System.currentTimeMillis();
        saveLong("Time:" + key, curTime);
    }

    static boolean isKeyTimeout(String key) {
        return isKeyTimeout(key, 1000 * 60 * 60 * 2);
    }

    private static boolean isKeyTimeout(String key, long duration) {
        long lastTime = getLong("Time:" + key, 0);
        long curTime = System.currentTimeMillis();

        return !(lastTime != 0 && curTime - lastTime < duration);
    }

    public static boolean isKeyPutToday(String key) {
        long lastTime = getLong("Time:" + key, 0);

        Calendar today = Calendar.getInstance();
        int y = today.get(Calendar.YEAR);
        int m = today.get(Calendar.MONTH);
        int d = today.get(Calendar.DAY_OF_MONTH);
        today.set(y, m, d, 0, 0, 0);

        return lastTime > today.getTimeInMillis();
    }

    /**
     * 版本升级后sp文件出现了变化，要删除之前所有的sp文件
     */
    public static void deleteAllSP() {
        boolean isDeleteAll = getBoolean("isDeleteAll", false);
        if (!isDeleteAll) {//之前的sp文件还没有删除
            String packageName = application.getPackageName();
            String SP_DIR = "/data/data/" + packageName + "/shared_prefs";

            SharedPreferences sp1 = application.getSharedPreferences("data_cache_CacheUtil", Activity.MODE_PRIVATE);
            sp1.edit().clear().commit();

            SharedPreferences sp2 = application.getSharedPreferences("forever_cache_CacheUtil", Activity.MODE_PRIVATE);
            sp2.edit().clear().commit();

            SharedPreferences sp3 = PreferenceManager.getDefaultSharedPreferences(application);
            sp3.edit().clear().commit();

            File file1 = new File(SP_DIR, "data_cache_CacheUtil.xml");
            if (file1.exists()) {
                file1.delete();
            }

            File file2 = new File(SP_DIR, "forever_cache_CacheUtil.xml");
            if (file2.exists()) {
                file2.delete();
            }

            File file3 = new File(SP_DIR, packageName + "_preferences.xml");
            if (file3.exists()) {
                file3.delete();
            }

            saveBoolean("isDeleteAll", true);
        }
    }
}
