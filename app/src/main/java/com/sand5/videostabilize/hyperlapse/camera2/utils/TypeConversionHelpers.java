package com.sand5.videostabilize.hyperlapse.camera2.utils;

import android.util.Size;

/**
 * Created by jeetdholakia on 1/11/17.
 */

public class TypeConversionHelpers {
    public static String sizesToString(Size[] sizes) {
        String result = "";
        if (sizes != null) {
            for (int j = 0; j < sizes.length; j++) {
                result += sizes[j].toString() + " ";
            }
        }
        return result;
    }

    public static String intsToString(int[] modes) {
        String result = "";
        if (modes != null) {
            for (int j = 0; j < modes.length; j++) {
                result += modes[j] + " ";
            }
        }
        return result;
    }

    public static String floatsToString(float[] modes) {
        String result = "";
        if (modes != null) {
            for (int j = 0; j < modes.length; j++) {
                result += modes[j] + " ";
            }
        }
        return result;
    }
}
