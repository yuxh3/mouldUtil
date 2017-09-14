package utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import java.util.List;

import common.AppConst;

/**
 * Created by yuxh3 on 2017/9/14.
 */

public class AppInfoTool {
    private static String mChannel;
    /**
     * 程序是否在前台运行
     *
     * @return
     */
    public static boolean isAppOnForeground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
        return !isInBackground;
    }
    /**
     * 返回市场。  如果获取失败返回""
     *
     * @param context
     * @return
     */
    public static String getChannel(Context context) {
        return getChannel(context, AppConst.CHANNEL_KEY);
    }
    /**
     * 返回市场。  如果获取失败返回defaultChannel
     *
     * @param context
     * @param defaultChannel
     * @return
     */
    public static String getChannel(Context context, String defaultChannel) {
        //内存中获取
        if (!TextUtils.isEmpty(mChannel)) {
            return mChannel;
        }
        //从apk中获取
//        mChannel = getChannelFromApk(context, defaultChannel);//这里面需要添加jar包
        if (!TextUtils.isEmpty(mChannel)) {
            return mChannel;
        }
        //全部获取失败
        return defaultChannel;
    }
}
