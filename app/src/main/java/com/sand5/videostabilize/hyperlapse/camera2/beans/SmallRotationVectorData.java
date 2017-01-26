package com.sand5.videostabilize.hyperlapse.camera2.beans;

/**
 * Created by jeetdholakia on 1/19/17.
 */

public class SmallRotationVectorData {

    private long timeStamp;
    private float[] rotationVector;
    private float[] rawRotationValues;

    public SmallRotationVectorData(long timeStamp, float[] rotationVector) {
        this.timeStamp = timeStamp;
        this.rotationVector = rotationVector;
    }

    public SmallRotationVectorData(long timeStamp, float[] rotationVector, float[] rawRotationValues) {
        this.timeStamp = timeStamp;
        this.rotationVector = rotationVector;
        this.rawRotationValues = rawRotationValues;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public float[] getRotationVector() {
        return rotationVector;
    }

    public void setRotationVector(float[] rotationVector) {
        this.rotationVector = rotationVector;
    }

    public float[] getRawRotationValues() {
        return rawRotationValues;
    }

    public void setRawRotationValues(float[] rawRotationValues) {
        this.rawRotationValues = rawRotationValues;
    }
}
