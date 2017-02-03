package com.sand5.videostabilize.hyperlapse.camera2.utils;

import com.orhanobut.logger.Logger;
import com.sand5.videostabilize.hyperlapse.camera2.beans.SynchronizedFrameTimeStamp;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by jeetdholakia on 1/16/17.
 */

public class RotationVectorUtils {

    public static float[] getEstimatedRotationVectors(long timeStamp) {
        // TODO: 1/21/17 Return nearest 2 values
        float[] vectorData = new float[0];
        LinkedHashMap<Long, float[]> rotationDataMap = RotationVectorDataStore.getAll();
        LinkedHashMap<Long, Mat> imageFrameMap = ImageFramesDataStore.getAll();
        Logger.d("ImageFrameMap Size:" + imageFrameMap.size());
        ArrayList<Long> rotationDataTimeStamps = new ArrayList<>(rotationDataMap.keySet());
        for (int i = 0; i < rotationDataTimeStamps.size(); i++) {
            Long vectorTimeStamp = rotationDataTimeStamps.get(i);
            if (timeStamp < vectorTimeStamp) {
                vectorData = rotationDataMap.get(vectorTimeStamp);
                if (imageFrameMap.containsKey(timeStamp)) {
                    Logger.d("ImageFrameMap contains the key");
                } else {
                    Logger.d("ImageFrameMap doesn't contain the key");
                }
                Mat testMat = imageFrameMap.get(timeStamp);
                if (null == testMat) {
                    Logger.d("RotationVectorUtil Mat null");
                } else {
                    Logger.d("RotationVectorUtil Mat not null");
                }
                SynchronizedFrameTimeStamp synchronizedFrameTimeStamp = new SynchronizedFrameTimeStamp(imageFrameMap.get(timeStamp), vectorData);
                SynchronizedFrameTimeStampDataStore.add(synchronizedFrameTimeStamp);
                break;
            }
        }
        return vectorData;
    }

    public static void printAll() {
        Logger.d("");
    }

}
