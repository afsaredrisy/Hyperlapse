package com.sand5.videostabilize.hyperlapse;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by jeetdholakia on 1/12/17.
 */

public class HyperlapseApplication extends Application {
    public static Bus bus = new Bus(ThreadEnforcer.ANY);

    public static Bus getBusInstance() {
        return bus;
    }

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
