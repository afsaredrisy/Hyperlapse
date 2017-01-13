package com.sand5.videostabilize.hyperlapse.camera2.utils;

import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;
import android.util.Size;
import android.util.SizeF;

/**
 * Created by jeetdholakia on 1/11/17.
 */

public class FOVCalculator {

    private static final String CAMERAPARAMETERSTAG = "Camera Parameters";

    public static double calculateFOV(CameraCharacteristics characteristics) {
        int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (cOrientation == CameraCharacteristics.LENS_FACING_BACK) {
            float[] maxFocus = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
            SizeF size = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
            float sensorWidth = size.getWidth();
            float sensorHeight = size.getHeight();
            float focalLength = maxFocus[0];
            double fov = Math.toDegrees(2 * Math.atan(0.5 * sensorWidth / focalLength));
            float horizontalAngle = (float) (2 * Math.atan(sensorWidth / (maxFocus[0] * 2)));
            float verticalAngle = (float) (2 * Math.atan(sensorHeight / (maxFocus[0] * 2)));
            Log.d(CAMERAPARAMETERSTAG, "Horizontal angle [a_y] : " + horizontalAngle + " vertical angle [a_x]: " + verticalAngle);
            return fov;
        } else {
            return 0;
        }
    }

    public static float[] calculateFOV2(CameraCharacteristics mCameraCharacteristics, AutoFitTextureView mTextureView) {
        SizeF physicalSize = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
        float[] focalLength = mCameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
        Size fullArraySize = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
        Rect activeRect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        Size outputStreamSize = new Size(mTextureView.getWidth(), mTextureView.getHeight());
        float[] angle = new float[2];
        angle[0] = 2f * (float) Math.toDegrees(Math.atan(physicalSize.getWidth() / (2 * focalLength[0]))); // 横
        angle[1] = 2f * (float) Math.toDegrees(Math.atan(physicalSize.getHeight() / (2 * focalLength[0]))); // 縦
        Log.d("FOV Angle", angle[0] + ", " + angle[1] + ", ");
        return angle;
    }
}
