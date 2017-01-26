package com.sand5.videostabilize.hyperlapse.camera2.utils;

import com.sand5.videostabilize.hyperlapse.camera2.beans.SmallRotationVectorData;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by jeetdholakia on 1/21/17.
 */

public class RotationVectorDataStore {

    private static LinkedHashMap<Long, float[]> rotationVectorDataMap = new java.util.LinkedHashMap<>();
    private static ArrayList<SmallRotationVectorData> smallRotationVectorDataArrayList = new ArrayList<>();

    public static void add(SmallRotationVectorData smallRotationVectorData) {
        rotationVectorDataMap.put(smallRotationVectorData.getTimeStamp(), smallRotationVectorData.getRotationVector());
    }

    public static LinkedHashMap<Long, float[]> getAll() {
        return rotationVectorDataMap;
    }

    public static ArrayList<SmallRotationVectorData> getAllInstances() {
        return smallRotationVectorDataArrayList;
    }

    public void getInstance(int key) {


    }

}
