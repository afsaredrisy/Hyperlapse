package com.sand5.videostabilize.hyperlapse.camera2.utils;

import com.orhanobut.logger.Logger;
import com.sand5.videostabilize.hyperlapse.camera2.beans.SynchronizedFrameTimeStamp;

import java.util.ArrayList;

/**
 * Created by jeetdholakia on 1/22/17.
 */

public class SynchronizedFrameTimeStampDataStore {

    private static ArrayList<SynchronizedFrameTimeStamp> synchronizedFrameTimeStampArrayList = new ArrayList<>();

    public static void add(SynchronizedFrameTimeStamp synchronizedFrameTimeStamp) {
        synchronizedFrameTimeStampArrayList.add(synchronizedFrameTimeStamp);
    }

    public static ArrayList<SynchronizedFrameTimeStamp> getSynchronizedFrameTimeStampArrayList() {
        return synchronizedFrameTimeStampArrayList;
    }

    public static void printAll() {
        Logger.d("Synchronized Frames Data-store size: " + synchronizedFrameTimeStampArrayList.size());
    }
}
