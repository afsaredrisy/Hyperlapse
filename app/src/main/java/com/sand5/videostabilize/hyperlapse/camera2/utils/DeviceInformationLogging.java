package com.sand5.videostabilize.hyperlapse.camera2.utils;

import android.app.Activity;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import github.nisrulz.easydeviceinfo.base.DeviceType;
import github.nisrulz.easydeviceinfo.base.EasyDeviceMod;
import github.nisrulz.easydeviceinfo.base.EasyDisplayMod;

/**
 * Created by jeetdholakia on 1/11/17.
 */

public class DeviceInformationLogging {

    private static final String TAG = "Device Information";

    /**
     * Print out various information about the device display.
     */
    public static void printDisplayInfo(Activity activity) {
        Log.d(TAG, "============= DEVICE  INFO =============");
        Log.d(TAG, "Build.DEVICE = " + Build.DEVICE);
        Log.d(TAG, "Build.FINGERPRINT = " + Build.FINGERPRINT);
        Log.d(TAG, "Build.BRAND = " + Build.BRAND);
        Log.d(TAG, "Build.MODEL = " + Build.MODEL);
        Log.d(TAG, "Build.PRODUCT = " + Build.PRODUCT);
        Log.d(TAG, "Build.MANUFACTURER = " + Build.MANUFACTURER);
        Log.d(TAG, "Build.VERSION.CODENAME = " + Build.VERSION.CODENAME);
        Log.d(TAG, "Build.VERSION.SDK_INT = " + Build.VERSION.SDK_INT);

        Log.d(TAG, "============= DEVICE DISPLAY INFO =============");
        WindowManager windowMgr = activity.getWindowManager();

        // Nexus 5 is 360dp * 567dp
        // Each dp is 3 hardware pixels
        Log.d(TAG, "screen width dp = " + activity.getResources().getConfiguration().screenWidthDp);
        Log.d(TAG, "screen height dp = " + activity.getResources().getConfiguration().screenHeightDp);

        DisplayMetrics metrics = new DisplayMetrics();
        // With chrome subtracted.
        windowMgr.getDefaultDisplay().getMetrics(metrics);
        Log.d(TAG, "screen width pixels = " + metrics.widthPixels);
        Log.d(TAG, "screen height pixels = " + metrics.heightPixels);
        // Native.
        windowMgr.getDefaultDisplay().getRealMetrics(metrics);
        Log.d(TAG, "real screen width pixels = " + metrics.widthPixels);
        Log.d(TAG, "real screen height pixels = " + metrics.heightPixels);

        Log.d(TAG, "refresh rate = " + windowMgr.getDefaultDisplay().getRefreshRate() + " Hz");


        Log.d(TAG, "Printing using easyDeviceMod");
        EasyDeviceMod easyDeviceMod = new EasyDeviceMod(activity.getApplicationContext());
        Log.d(TAG, "ScreenDisplayID" + easyDeviceMod.getScreenDisplayID());
        Log.d(TAG, "getBuildVersionCodename" + easyDeviceMod.getBuildVersionCodename());
        Log.d(TAG, "getBuildVersionIncremental" + easyDeviceMod.getBuildVersionIncremental());
        Log.d(TAG, "getBuildVersionSDK" + easyDeviceMod.getBuildVersionSDK());
        Log.d(TAG, "getBuildID" + easyDeviceMod.getBuildID());
        Log.d(TAG, "getBuildBrand" + easyDeviceMod.getBuildBrand());
        Log.d(TAG, "getManufacturer" + easyDeviceMod.getManufacturer());
        Log.d(TAG, "getModel" + easyDeviceMod.getModel());
        Log.d(TAG, "getOSCodename" + easyDeviceMod.getOSCodename());
        Log.d(TAG, "getOSVersion" + easyDeviceMod.getOSVersion());
        Log.d(TAG, "getProduct" + easyDeviceMod.getProduct());
        Log.d(TAG, "getDevice" + easyDeviceMod.getDevice());
        Log.d(TAG, "getBoard" + easyDeviceMod.getBoard());
        Log.d(TAG, "getBootloader" + easyDeviceMod.getBootloader());
        Log.d(TAG, "isDeviceRooted" + easyDeviceMod.isDeviceRooted());
        Log.d(TAG, "getBuildHost" + easyDeviceMod.getBuildHost());
        Log.d(TAG, "getBuildTags" + easyDeviceMod.getBuildTags());
        Log.d(TAG, "getBuildTime" + easyDeviceMod.getBuildTime());
        Log.d(TAG, "getBuildUser" + easyDeviceMod.getBuildUser());
        Log.d(TAG, "getBuildVersionRelease" + easyDeviceMod.getBuildVersionRelease());
        Log.d(TAG, "getFingerprint" + easyDeviceMod.getFingerprint());

        @DeviceType
        int deviceType = easyDeviceMod.getDeviceType(activity);
        switch (deviceType) {
            case DeviceType.WATCH:
                Log.d(TAG, "watch");
                break;
            case DeviceType.PHONE:
                Log.d(TAG, "phone");
                break;
            case DeviceType.PHABLET:
                Log.d(TAG, "phablet");
                break;
            case DeviceType.TABLET:
                Log.d(TAG, "tablet");
                break;
            case DeviceType.TV:
                Log.d(TAG, "tv");
                break;
        }

        EasyDisplayMod easyDisplayMod = new EasyDisplayMod(activity);
        Log.d(TAG, "getResolution" + easyDisplayMod.getResolution());
        Log.d(TAG, "getDensity" + easyDisplayMod.getDensity());
        Log.d(TAG, "getRefreshRate" + easyDisplayMod.getRefreshRate());
        Log.d(TAG, "getPhysicalSize" + easyDisplayMod.getPhysicalSize());
    }

}
