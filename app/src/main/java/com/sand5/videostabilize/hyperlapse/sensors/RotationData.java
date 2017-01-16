package com.sand5.videostabilize.hyperlapse.sensors;

/**
 * Created by jeetdholakia on 1/15/17.
 */

public class RotationData {

    private long systemCurrentTimeMillis;
    private long nanoTime;
    private long elapsedRealtimeNanos;
    private long RotationTimeStamp;
    private float[] RotationEvent;

    public RotationData(long systemCurrentTimeMillis, long nanoTime, long elapsedRealtimeNanos, long RotationTimeStamp, float[] RotationEvent) {
        this.systemCurrentTimeMillis = systemCurrentTimeMillis;
        this.nanoTime = nanoTime;
        this.elapsedRealtimeNanos = elapsedRealtimeNanos;
        this.RotationTimeStamp = RotationTimeStamp;
        this.RotationEvent = RotationEvent;
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

    public float[] getRotationEvent() {
        return RotationEvent;
    }

    public void setRotationEvent(float[] RotationEvent) {
        this.RotationEvent = RotationEvent;
    }

    public long getDetlaToNano() {
        return nanoTime - RotationTimeStamp;
    }

    public long getDeltaToRealTimeElapsed() {
        return elapsedRealtimeNanos - RotationTimeStamp;
    }

}
