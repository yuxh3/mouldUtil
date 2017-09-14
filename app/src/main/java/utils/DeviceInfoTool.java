package utils;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

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
    /**
     * 手机品牌
     */
    public static String getBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 手机型号
     */
    public static String getModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取设备分辨率
     */
    public static String getScreenResolution() {
        return getRealScreenWidth() + "x" + getRealScreenHeight();
    }
    public static int getRealScreenWidth() {
        if (mRealScreenSize == null) {
            initScreenSize();
        }
        return mRealScreenSize.x;
    }

    public static int getRealScreenHeight() {
        if (mRealScreenSize == null) {
            initScreenSize();
        }
        return mRealScreenSize.y;
    }
    private static void initScreenSize() {
        mScreenSize = new Point();
        mRealScreenSize = new Point();
        Display display = ((WindowManager) application.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getSize(mScreenSize);
        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(mRealScreenSize);
        } else /*if (Build.VERSION.SDK_INT >= 14) */ {
            //reflection for this weird in-between time
            try {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                int x = (Integer) mGetRawW.invoke(display);
                int y = (Integer) mGetRawH.invoke(display);
                mRealScreenSize.set(x, y);
            } catch (Exception e) {
                //this may not be 100% accurate, but it's all we've got
                display.getSize(mRealScreenSize);
            }
        } /*else {
            //This should be close, as lower API devices should not have window navigation bars
            realWidth = display.getWidth();
            realHeight = display.getHeight();
        }*/
    }
    /**
     * 获取 设备/Rom 生产厂家
     *
     * @return
     */
    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * 获取设备显示的Rom版本号
     *
     * @return
     */
    public static String getDisplayVersion() {
        return Build.DISPLAY;
    }

    /**
     * 获取系统的Android版本号
     *
     * @return
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 运营商名称,注意：仅当用户已在网络注册时有效,在CDMA网络中结果也许不可靠
     */
    public static String getNetworkOperatorName() {
        return telephonyManager.getNetworkOperatorName();
    }
    public static PackageManager getPackageManager() {
        return application.getPackageManager();
    }
    /**
     * 获取当前渠道
     */
    public static String getChannel() {
        return AppInfoTool.getChannel(application);
    }
    /**
     * 获取当前程序的包信息
     */
    public static PackageInfo getPackageInfo() {
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(application.getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packInfo;
    }
    /**
     * 获取原生的版本名称
     */
    public static String getAppVersion() {
        return getPackageInfo().versionName;
    }

    /**
     * 版本号特殊处理，截取后的大版本号给接口
     * 例如2.1.3为大的版本号，2.1.3.1为小版本号
     *
     * @return
     */
    public static String getSpecialVersion() {
        String vsName = getPackageInfo().versionName;
        int length;
        String[] split;
        try {
            split = vsName.split("\\.");
            length = split.length;
        } catch (Exception e) {
            return vsName;
        }
        StringBuffer sb = new StringBuffer();
        if (length < 3) {
            return vsName;
        } else {
            sb.append(split[0])
                    .append(".")
                    .append(split[1])
                    .append(".")
                    .append(split[2]);
        }
        return sb.toString();
    }
    /**
     * 获取当前的系统给的版本code
     */
    public static int getVersionCode() {
        return getPackageInfo().versionCode;
    }

    /**
     * 检查网络状况
     * 返回需要带有提示语
     *
     * @return
     */
    public static boolean checkNetwork(Context mContext) {
        if (isNetworkAvailable(mContext)) {
            return true;
        } else {
            Toast.makeText(mContext,"网络不行", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    /**
     * 检测网络连接是否可用
     * 不需要使用提示语
     *
     * @return true 可用; false 不可用
     */
    public static boolean isNetworkAvailable(Context mContext) {
        if(mContext==null){
            return true;
        }
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo[] netinfo = null;
        try {
            if (null != cm.getAllNetworkInfo()) {
                netinfo = cm.getAllNetworkInfo();
            } else {
                return false;
            }
        } catch (Exception e) {
        }
        if (netinfo == null) {
            return false;
        }
        for (NetworkInfo aNetinfo : netinfo) {
            if (aNetinfo.isConnected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取手机app应用的通知权限是否开启的状态
     * 1 标示开启true
     * 2 表示关闭false
     */
    public static int isNotificationEnabled(Context context) {
        if (context == null) {
            return 1;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            ApplicationInfo appInfo = context.getApplicationInfo();
            String pkg = context.getApplicationContext().getPackageName();
            int uid = appInfo.uid;
            try {
                Class appOpsClass = Class.forName(AppOpsManager.class.getName());
                Method checkOpNoThrowMethod = appOpsClass.getMethod("checkOpNoThrow", Integer.TYPE, Integer.TYPE, String.class);
                Field opPostNotificationValue = appOpsClass.getDeclaredField("OP_POST_NOTIFICATION");
                int value = (int) opPostNotificationValue.get(Integer.class);
                if (((int) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED)) {
                    return 1;
                } else {
                    return 2;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return 1;
        }
        return 2;
    }
    /**
     * 判断是否是MIUI V8系统
     *
     * @return true
     */
    public static boolean isMIUIV8() {
        InputStream inputStream = null;
        try {
            if (mIsJudgement) {
                Properties properties = new Properties();
                File file = new File(Environment.getRootDirectory(), "build.prop");
                inputStream = new FileInputStream(file);
                properties.load(inputStream);
                String miuiCode = properties.getProperty("ro.miui.ui.version.code", null);
                String miuiName = properties.getProperty("ro.miui.ui.version.name", null);
                if (TextUtils.equals("6", miuiCode) && TextUtils.equals("V8", miuiName)) {
                    return true;
                }
                mIsJudgement = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}
