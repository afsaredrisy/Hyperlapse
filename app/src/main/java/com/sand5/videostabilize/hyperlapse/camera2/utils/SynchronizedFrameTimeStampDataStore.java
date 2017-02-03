package com.sand5.videostabilize.hyperlapse.camera2.utils;

import com.orhanobut.logger.Logger;
import com.sand5.videostabilize.hyperlapse.camera2.beans.SynchronizedFrameTimeStamp;

import org.opencv.core.Mat;

import java.util.ArrayList;

/**
 * Created by jeetdholakia on 1/22/17.
 */

public class SynchronizedFrameTimeStampDataStore {

    private static ArrayList<SynchronizedFrameTimeStamp> synchronizedFrameTimeStampArrayList = new ArrayList<>();

    public static void add(SynchronizedFrameTimeStamp synchronizedFrameTimeStamp) {
        Mat testMat = synchronizedFrameTimeStamp.getMat();
        if (null == testMat) {
            Logger.d("Synchronized Mat null");
        } else {
            Logger.d("Synchronized Mat not null");
        }
        synchronizedFrameTimeStampArrayList.add(synchronizedFrameTimeStamp);
    }

    public static ArrayList<SynchronizedFrameTimeStamp> getSynchronizedFrameTimeStampArrayList() {
        return synchronizedFrameTimeStampArrayList;
    }

    public static void printAll() {
        Logger.d("Synchronized Frames Data-store size: " + synchronizedFrameTimeStampArrayList.size());
    }
}
