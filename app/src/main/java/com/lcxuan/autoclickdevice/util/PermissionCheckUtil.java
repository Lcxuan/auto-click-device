package com.lcxuan.autoclickdevice.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.util.List;

public class PermissionCheckUtil {

    /**
     * 判断是否开启无障碍服务
     * @param context 上下文
     * @param className 包名
     * @return
     */
    public static boolean isAccessibility(Context context, String className){
        ActivityManager activityManager = (ActivityManager)context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo>
                runningServices = activityManager.getRunningServices(100);
        if (runningServices.size() < 0 ){
            return false;
        }
        for (int i = 0;i<runningServices.size();i++){
            ComponentName service = runningServices.get(i).service;
            if (service.getClassName().contains(className)){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否开启悬浮窗
     * @param context 上下文
     * @return
     */
    public static boolean isSuspensionWindow(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(context)){
                return false;
            }
            return true;
        }
        return false;
    }

}
