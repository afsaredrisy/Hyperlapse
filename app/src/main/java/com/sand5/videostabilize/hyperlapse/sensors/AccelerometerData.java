package com.sand5.videostabilize.hyperlapse.sensors;

/**
 * Created by jeetdholakia on 1/15/17.
 */

public class AccelerometerData {

    private long systemCurrentTimeMillis;
    private long nanoTime;
    private long elapsedRealtimeNanos;
    private long accelerometerTimeStamp;
    private float[] accelerometerEvent;

    public AccelerometerData(long systemCurrentTimeMillis, long nanoTime, long elapsedRealtimeNanos, long accelerometerTimeStamp, float[] accelerometerEvent) {
        this.systemCurrentTimeMillis = systemCurrentTimeMillis;
        this.nanoTime = nanoTime;
        this.elapsedRealtimeNanos = elapsedRealtimeNanos;
        this.accelerometerTimeStamp = accelerometerTimeStamp;
        this.accelerometerEvent = accelerometerEvent;
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

    public long getAccelerometerTimeStamp() {
        return accelerometerTimeStamp;
    }

    public void setAccelerometerTimeStamp(long accelerometerTimeStamp) {
        this.accelerometerTimeStamp = accelerometerTimeStamp;
    }

    public float[] getAccelerometerEvent() {
        return accelerometerEvent;
    }

    public void setAccelerometerEvent(float[] accelerometerEvent) {
        this.accelerometerEvent = accelerometerEvent;
    }

    public long getDetlaToNano() {
        return nanoTime - accelerometerTimeStamp;
    }

    public long getDeltaToRealTimeElapsed() {
        return elapsedRealtimeNanos - accelerometerTimeStamp;
    }
}
