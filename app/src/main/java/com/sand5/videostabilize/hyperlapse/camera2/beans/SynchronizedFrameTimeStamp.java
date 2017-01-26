package com.sand5.videostabilize.hyperlapse.camera2.beans;

import org.opencv.core.Mat;

/**
 * Created by jeetdholakia on 1/19/17.
 */

public class SynchronizedFrameTimeStamp {

    private Mat mat;
    private float[] rotationMatrix;

    public SynchronizedFrameTimeStamp(Mat mat, float[] rotationMatrix) {
        this.mat = mat;
        this.rotationMatrix = rotationMatrix;
    }

    public Mat getMat() {
        return mat;
    }

    public void setMat(Mat mat) {
        this.mat = mat;
    }

    public float[] getRotationMatrix() {
        return rotationMatrix;
    }

    public void setRotationMatrix(float[] rotationMatrix) {
        this.rotationMatrix = rotationMatrix;
    }
}
