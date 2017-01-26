package com.sand5.videostabilize.hyperlapse.camera2.utils;

import com.sand5.videostabilize.hyperlapse.camera2.beans.FrameMat;

import org.opencv.core.Mat;

import java.util.LinkedHashMap;

/**
 * Created by jeetdholakia on 1/21/17.
 */

public class ImageFramesDataStore {

    private static LinkedHashMap<Long, Mat> imageFrameMats = new LinkedHashMap<>();

    public static void add(FrameMat frameMat) {
        imageFrameMats.put(frameMat.getTimeStamp(), frameMat.getMat());
    }

    public static LinkedHashMap<Long, Mat> getAll() {
        return imageFrameMats;
    }


}
