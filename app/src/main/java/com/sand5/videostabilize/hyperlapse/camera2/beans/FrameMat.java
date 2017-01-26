package com.sand5.videostabilize.hyperlapse.camera2.beans;

import org.opencv.core.Mat;

/**
 * Created by jeetdholakia on 1/19/17.
 */

public class FrameMat {

    long timeStamp;
    Mat mat;

    public FrameMat(long timeStamp, Mat mat) {
        this.timeStamp = timeStamp;
        this.mat = mat;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Mat getMat() {
        return mat;
    }

    public void setMat(Mat mat) {
        this.mat = mat;
    }
}
