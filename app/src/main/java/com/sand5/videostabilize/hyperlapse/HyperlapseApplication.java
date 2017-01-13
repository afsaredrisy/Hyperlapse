package com.sand5.videostabilize.hyperlapse;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by jeetdholakia on 1/12/17.
 */

public class HyperlapseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        // Normal app init code...
    }
}
