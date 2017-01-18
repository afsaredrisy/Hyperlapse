package com.sand5.videostabilize.hyperlapse.camera2.beans;

/**
 * Created by jeetdholakia on 1/18/17.
 */

public class IntrinsicMatrix {

    private long rollingShutterSkew;
    private float focalLength;
    private float focusDistance;
    private float[] focalLengthAngles;
    private float[] principlePoints;

    public IntrinsicMatrix(Long rollingShutterSkew, float focalLength, float focusDistance, float[] focalLengthAngles, float[] principlePoints) {
        this.rollingShutterSkew = rollingShutterSkew;
        this.focalLength = focalLength;
        this.focusDistance = focusDistance;
        this.focalLengthAngles = focalLengthAngles;
        this.principlePoints = principlePoints;
    }

    public Long getRollingShutterSkew() {
        return rollingShutterSkew;
    }

    public void setRollingShutterSkew(Long rollingShutterSkew) {
        this.rollingShutterSkew = rollingShutterSkew;
    }

    public float getFocalLength() {
        return focalLength;
    }

    public void setFocalLength(float focalLength) {
        this.focalLength = focalLength;
    }

    public float getFocusDistance() {
        return focusDistance;
    }

    public void setFocusDistance(float focusDistance) {
        this.focusDistance = focusDistance;
    }

    public float[] getFocalLengthAngles() {
        return focalLengthAngles;
    }

    public void setFocalLengthAngles(float[] focalLengthAngles) {
        this.focalLengthAngles = focalLengthAngles;
    }

    public float[] getPrinciplePoints() {
        return principlePoints;
    }

    public void setPrinciplePoints(float[] principlePoints) {
        this.principlePoints = principlePoints;
    }
}
