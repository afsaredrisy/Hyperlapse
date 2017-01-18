package com.sand5.videostabilize.hyperlapse.camera2.beans;

/**
 * Created by jeetdholakia on 1/15/17.
 */

public class RotationVectorData {

    private long systemCurrentTimeMillis;
    private long nanoTime;
    private long elapsedRealtimeNanos;
    private long RotationTimeStamp;
    private float[] RotationVectorEvent;
    private float[] RotationRawEvent;

    public RotationVectorData(long systemCurrentTimeMillis, long nanoTime, long elapsedRealtimeNanos, long RotationTimeStamp, float[] rotationRawEvent, float[] RotationVectorEvent) {
        this.systemCurrentTimeMillis = systemCurrentTimeMillis;
        this.nanoTime = nanoTime;
        this.elapsedRealtimeNanos = elapsedRealtimeNanos;
        this.RotationTimeStamp = RotationTimeStamp;
        this.RotationRawEvent = rotationRawEvent;
        this.RotationVectorEvent = RotationVectorEvent;
    }

    public long getSystemCurrentTimeMillis() {
        return systemCurrentTimeMillis;
    }

    public void setSystemCurrentTimeMillis(long systemCurrentTimeMillis) {
        this.systemCurrentTimeMillis = systemCurrentTimeMillis;
    }

    public long getNanoTime() {
        return nanoTime;
    }

    public void setNanoTime(long nanoTime) {
        this.nanoTime = nanoTime;
    }

    public long getElapsedRealtimeNanos() {
        return elapsedRealtimeNanos;
    }

    public void setElapsedRealtimeNanos(long elapsedRealtimeNanos) {
        this.elapsedRealtimeNanos = elapsedRealtimeNanos;
    }

    public long getRotationTimeStamp() {
        return RotationTimeStamp;
    }

    public void setRotationTimeStamp(long RotationTimeStamp) {
        this.RotationTimeStamp = RotationTimeStamp;
    }

    public float[] getRotationVectorEvent() {
        return RotationVectorEvent;
    }

    public void setRotationVectorEvent(float[] RotationEvent) {
        this.RotationVectorEvent = RotationEvent;
    }

    public long getDetlaToNano() {
        return nanoTime - RotationTimeStamp;
    }

    public long getDeltaToRealTimeElapsed() {
        return elapsedRealtimeNanos - RotationTimeStamp;
    }

    public float[] getRotationRawEvent() {
        return RotationRawEvent;
    }

    public void setRotationRawEvent(float[] rotationRawEvent) {
        RotationRawEvent = rotationRawEvent;
    }
}
