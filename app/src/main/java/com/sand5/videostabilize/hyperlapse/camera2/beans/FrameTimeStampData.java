package com.sand5.videostabilize.hyperlapse.camera2.beans;

/**
 * Created by jeetdholakia on 1/16/17.
 */

public class FrameTimeStampData {

    private long systemCurrentTimeMillis;
    private long nanoTime;
    private long elapsedRealtimeNanos;
    private long frameTimeStamp;

    public FrameTimeStampData(long systemCurrentTimeMillis, long nanoTime, long elapsedRealtimeNanos, long frameTimeStamp) {
        this.systemCurrentTimeMillis = systemCurrentTimeMillis;
        this.nanoTime = nanoTime;
        this.elapsedRealtimeNanos = elapsedRealtimeNanos;
        this.frameTimeStamp = frameTimeStamp;
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

    public long getFrameTimeStamp() {
        return frameTimeStamp;
    }

    public void setFrameTimeStamp(long frameTimeStamp) {
        this.frameTimeStamp = frameTimeStamp;
    }
}
