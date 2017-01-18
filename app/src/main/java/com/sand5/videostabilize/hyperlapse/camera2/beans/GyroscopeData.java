package com.sand5.videostabilize.hyperlapse.camera2.beans;

/**
 * Created by jeetdholakia on 1/16/17.
 */

public class GyroscopeData {

    private long systemCurrentTimeMillis;
    private long nanoTime;
    private long elapsedRealtimeNanos;
    private long GyroscopeTimeStamp;
    private float[] GyroscopeEvent;

    public GyroscopeData(long systemCurrentTimeMillis, long nanoTime, long elapsedRealtimeNanos, long GyroscopeTimeStamp, float[] GyroscopeEvent) {
        this.systemCurrentTimeMillis = systemCurrentTimeMillis;
        this.nanoTime = nanoTime;
        this.elapsedRealtimeNanos = elapsedRealtimeNanos;
        this.GyroscopeTimeStamp = GyroscopeTimeStamp;
        this.GyroscopeEvent = GyroscopeEvent;
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

    public long getGyroscopeTimeStamp() {
        return GyroscopeTimeStamp;
    }

    public void setGyroscopeTimeStamp(long GyroscopeTimeStamp) {
        this.GyroscopeTimeStamp = GyroscopeTimeStamp;
    }

    public float[] getGyroscopeEvent() {
        return GyroscopeEvent;
    }

    public void setGyroscopeEvent(float[] GyroscopeEvent) {
        this.GyroscopeEvent = GyroscopeEvent;
    }

    public long getDetlaToNano() {
        return nanoTime - GyroscopeTimeStamp;
    }

    public long getDeltaToRealTimeElapsed() {
        return elapsedRealtimeNanos - GyroscopeTimeStamp;
    }
}
