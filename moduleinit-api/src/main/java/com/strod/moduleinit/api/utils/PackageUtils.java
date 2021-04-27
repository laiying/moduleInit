package com.strod.moduleinit.api.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Android package utils
 *
 */
public class PackageUtils {
    private static String NEW_VERSION_NAME;
    private static int NEW_VERSION_CODE;

    public static boolean isNewVersion(Context context) {
        PackageInfo packageInfo = getPackageInfo(context);
        if (null != packageInfo) {
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;
            SharedPreferences sp = context.getSharedPreferences(ConfigConstants.MODULE_INIT_SP_CACHE_KEY, Context.MODE_PRIVATE);
            if (!versionName.equals(sp.getString(ConfigConstants.LAST_VERSION_NAME, null)) ||
                    versionCode != sp.getInt(ConfigConstants.LAST_VERSION_CODE, -1)) {
                // new version
                NEW_VERSION_NAME = versionName;
                NEW_VERSION_CODE = versionCode;

                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static void updateVersion(Context context) {
        if (!android.text.TextUtils.isEmpty(NEW_VERSION_NAME) && NEW_VERSION_CODE != 0) {
            SharedPreferences sp = context.getSharedPreferences(ConfigConstants.MODULE_INIT_SP_CACHE_KEY, Context.MODE_PRIVATE);
            sp.edit().putString(ConfigConstants.LAST_VERSION_NAME, NEW_VERSION_NAME).putInt(ConfigConstants.LAST_VERSION_CODE, NEW_VERSION_CODE).apply();
        }
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
        } catch (Exception ex) {
            Log.e("PackageUtils", "Get package info error.");
        }

        return packageInfo;
    }
}
