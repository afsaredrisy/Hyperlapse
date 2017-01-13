package com.sand5.videostabilize.hyperlapse.camera2.utils;

/**
 * Created by jeetdholakia on 1/13/17.
 */

public class SensorLoggingUtils {
    /*private static final String SENSORSTAG = "SensorsParameters";
    private float[] mRotationMatrix = new float[16];
    mRotationMatrix[0] = 1;
    mRotationMatrix[5] = 1;
    mRotationMatrix[10] = 1;
    mRotationMatrix[15] = 1;

    public static void logGyroScopeValues(SensorEvent event){
        Log.d(SENSORSTAG,"Accelerometer Timestamp:" + event.timestamp);
        logSensorValues("Accelerometer", event);
    }

    public static void logAcceleremeterValues(SensorEvent event){
        Log.d(SENSORSTAG,"Accelerometer Timestamp:" + event.timestamp);
        logSensorValues("Accelerometer", event);
    }

    public static void logRotationVectorValues(SensorEvent event){
        Log.d(SENSORSTAG,"Accelerometer Timestamp:" + event.timestamp);
        SensorManager.getRotationMatrixFromVector(
                mRotationMatrix , event.values);
        Log.d(SENSORSTAG,"Rotation Matrix: " + Arrays.toString(mRotationMatrix));
        logSensorValues("Accelerometer", event);
    }


    private static void logSensorValues(String sensorType, SensorEvent event) {
        StringBuffer sb = new StringBuffer(sensorType + "values: ");
        for (int i = 0; i < event.values.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(event.values[i]);
        }
        Log.d(SENSORSTAG, "onSensorChanged " + sb.toString());
    }*/

}
