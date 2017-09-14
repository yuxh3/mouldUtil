package utils;

import android.Manifest;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import application.BaseApplication;

/**
 * Created by yuxh3 on 2017/9/14.
 */

public class DeviceInfoTool {

    private static BaseApplication application = BaseApplication.getInstance();
    private static TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(Context.TELEPHONY_SERVICE);
    private static boolean mIsJudgement = true;
    private static Point mScreenSize;
    private static Point mRealScreenSize;
    /**
     * 获取macid
     */
    public static String getMacId() {
        String str = "";
        String macSerial = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            try {
                Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
                InputStreamReader ir = new InputStreamReader(pp.getInputStream());
                LineNumberReader input = new LineNumberReader(ir);
                for (; null != str; ) {
                    str = input.readLine();
                    if (str != null) {
                        macSerial = str.trim();// 去空格
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (TextUtils.isEmpty(macSerial)) {
                try {
                    return loadFileAsString("/sys/class/net/eth0/address")
                            .toUpperCase().substring(0, 17);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return macSerial;
    }

    /**
     * ANDROID_ID可以作为设备标识，但需要注意：
     * <p>
     * 厂商定制系统的Bug：不同的设备可能会产生相同的ANDROID_ID：9774d56d682e549c。
     * 厂商定制系统的Bug：有些设备返回的值为null。
     * 设备差异：对于CDMA设备，ANDROID_ID和TelephonyManager.getDeviceId() 返回相同的值。
     *
     * @return
     */
    public static String getAndroidID() {
        return Settings.System.getString(BaseApplication.getInstance().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private static String loadFileAsString(String fileName) {
        StringBuilder builder = new StringBuilder();
        FileReader reader = null;
        try {
            reader = new FileReader(fileName);
            char[] buffer = new char[4096];
            int readLength = reader.read(buffer);
            while (readLength >= 0) {
                builder.append(buffer, 0, readLength);
                readLength = reader.read(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }
    /**
     * 手机串号:GSM手机的 IMEI 和 CDMA手机的 MEID
     */
    public static String getDeviceId() {
        //6.0手机需要检测权限
        if (PermissionsTool.checkPermission(BaseApplication.getInstance(), Manifest.permission.READ_PHONE_STATE)) {
            return telephonyManager.getDeviceId();
        } else {
            return "";
        }
    }
    /**
     * 判断DeviceID是否合法
     *
     * @param deviceId
     * @return true 非法，false 合法
     */
    public static boolean isDeviceIdIllegal(String deviceId) {
        return TextUtils.isEmpty(deviceId) || deviceId.length() < 5;
    }
    /**
     * 判断Mac地址是否合法
     *
     * @param macId
     * @return true 非法，false 合法
     */
    public static boolean isMacIdIllegal(String macId) {
        return TextUtils.isEmpty(macId) || macId.length() < 10 || TextUtils.equals(macId, "02:00:00:00:00:00");
    }
    /**
     * 获取Sim卡序列号
     *
     * @return
     */
    public static String getSimNumber() {
        //6.0手机需要检测权限
        if (PermissionsTool.checkPermission(BaseApplication.getInstance(), Manifest.permission.READ_PHONE_STATE)) {
            return telephonyManager.getSimSerialNumber();
        } else {
            return "";
        }
    }
    /**
     * 获取手机序列号
     *
     * @return
     */
    public static String getSerialNumber() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return Build.SERIAL;
        }
        //8.0手机需要检测权限
        if (PermissionsTool.checkPermission(BaseApplication.getInstance(), Manifest.permission.READ_PHONE_STATE)) {
            return Build.getSerial();
        }
        return "";

    }
}
