package com.sand5.videostabilize.hyperlapse.camera2.utils;

import com.orhanobut.logger.Logger;
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
        Mat testMat = frameMat.getMat();
        if (null == testMat) {
            Logger.d("ImageDataStore Mat null");
        } else {
            Logger.d("ImageDataStore Mat not null");
        }
        //Logger.d("Image Data store size:" + imageFrameMats.size());
    }
    public static LinkedHashMap<Long, Mat> getAll() {
        return imageFrameMats;
    }
}
