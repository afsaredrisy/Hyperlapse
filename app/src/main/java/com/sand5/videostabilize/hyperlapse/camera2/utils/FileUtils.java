package com.sand5.videostabilize.hyperlapse.camera2.utils;

import android.content.Context;
import android.os.Environment;

/**
 * Created by jeetdholakia on 1/17/17.
 */

public class FileUtils {

    public static String getVideoFilePath(Context context) {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath() + "/"
                + System.currentTimeMillis() + ".mp4";
    }

}
