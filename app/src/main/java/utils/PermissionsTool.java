package utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

/**
 * Created by yuxh3 on 2017/9/14.
 */

public class PermissionsTool {

    // 状态码、标志位
    private static final int REQUEST_STATUS_CODE = 0x001;
    private static final int REQUEST_PERMISSION_SETTING = 0x002;
    //常量字符串数组，将需要申请的权限写进去，同时必须要在Androidmanifest.xml中声明。
    private static String[] PERMISSIONS_GROUP = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };
    /**
     * 检测权限
     * 有未授权权限返回false，已授权返回true
     *
     * @param context
     * @param perName
     * @return
     */
    public static boolean checkPermission(Context context, String perName) {
        int permissionCheck = ContextCompat.checkSelfPermission(context, perName);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }
    /**
     * * 检测权限
     * 有未授权权限返回false，已授权返回true
     *
     * @param context
     * @param permissionsName
     * @return
     */
    public static boolean checkPermissions(final Context context, String permissionsName) {
        int grantCode = ActivityCompat.checkSelfPermission(context, permissionsName);
        return grantCode == PackageManager.PERMISSION_GRANTED;
    }
    /**
     * 有未授权权限返回false，已授权返回true
     *
     * @param context
     * @param permissionsGroup
     * @return
     */
    public static boolean checkPermissions(final Context context, String[] permissionsGroup) {
        // 一个list，用来存放没有被授权的权限
        ArrayList<String> denidArray = new ArrayList<>();

        // 遍历PERMISSIONS_GROUP，将没有被授权的权限存放进denidArray
        for (String permission : permissionsGroup) {
            int grantCode = ActivityCompat.checkSelfPermission(context, permission);
            if (grantCode == PackageManager.PERMISSION_DENIED) {
                denidArray.add(permission);
            }
        }
        // 将denidArray转化为字符串数组，方便下面调用requestPermissions来请求授权
        String[] denidPermissions = denidArray.toArray(new String[denidArray.size()]);
        // 如果该字符串数组长度大于0，说明有未被授权的权限
        return denidPermissions.length <= 0;
    }
    /**
     * 特殊权限特殊处理
     *
     * @param context
     */
    public static void checkAndRequestWriteSetting(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }
    /**
     * 对权限字符串数组中的所有权限进行申请授权，如果用户选择了“dont ask me again”，则不会弹出系统的Permission申请授权对话框
     */
    public static void requestPermissions(Activity activity, String[] permissions) {
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_STATUS_CODE);
    }

    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }
    /**
     * 关于shouldShowRequestPermissionRationale函数的一点儿注意事项：
     * ***1).应用安装后第一次访问，则直接返回false；
     * ***2).第一次请求权限时，用户Deny了，再次调用shouldShowRequestPermissionRationale()，则返回true；
     * ***3).第二次请求权限时，用户Deny了，并选择了“dont ask me again”的选项时，再次调用shouldShowRequestPermissionRationale()时，返回false；
     * ***4).设备的系统设置中，禁止了应用获取这个权限的授权，则调用shouldShowRequestPermissionRationale()，返回false。
     */
    public static boolean showRationaleUI(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }


    public static boolean checkAndRequestPermissions(final Activity activity) {

        // 一个list，用来存放没有被授权的权限
        ArrayList<String> denidArray = new ArrayList<>();

        // 遍历PERMISSIONS_GROUP，将没有被授权的权限存放进denidArray
        for (String permission : PERMISSIONS_GROUP) {
            int grantCode = ActivityCompat.checkSelfPermission(activity, permission);
            if (grantCode == PackageManager.PERMISSION_DENIED) {
                denidArray.add(permission);
            }
        }

        // 将denidArray转化为字符串数组，方便下面调用requestPermissions来请求授权
        String[] denidPermissions = denidArray.toArray(new String[denidArray.size()]);

        // 如果该字符串数组长度大于0，说明有未被授权的权限
        if (denidPermissions.length > 0) {
            // 遍历denidArray，用showRationaleUI来判断，每一个没有得到授权的权限是否是用户手动拒绝的
            for (String permission : denidArray) {
                // 如果permission是用户手动拒绝的，则用SnackBar来引导用户进入App设置页面，手动授予权限
                if (!showRationaleUI(activity, permission)) {
                    // 判断App是否是首次启动
                   /* if (!isAppFirstRun(activity)) {
                        Snackbar snackbar =
                                Snackbar.make(activity.findViewById(R.id.btn_sdcard_avail), "这次是真的需要去授权了",
                                        Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("前往设置", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 进入App设置页面
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                                intent.setData(uri);
                                activity.startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                            }
                        });
                        snackbar.show();
                    }*/
                }
            }
            requestPermissions(activity, denidPermissions);
            return true;
        }
        return false;
    }
}
