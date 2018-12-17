package com.hrl.chaui.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;


/**
 * @author huangrui
  * @desc
 */
public class PermissionUtil {

    /**
     * 得到上下文
     *
     * @return
     */
    public static void  startSetPermissionActivity(Context mContetx) {
        Intent intent = new Intent();
    //    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package","com.rance.chatui", null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", "com.rance.chatui");
        }

        ((Activity)mContetx).startActivity (intent);
    }
}
