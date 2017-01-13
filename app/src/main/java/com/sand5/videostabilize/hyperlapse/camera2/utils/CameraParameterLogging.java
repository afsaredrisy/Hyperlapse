package com.sand5.videostabilize.hyperlapse.camera2.utils;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SizeF;

import java.util.Arrays;

import static com.google.android.gms.internal.zzs.TAG;
import static com.sand5.videostabilize.hyperlapse.camera2.utils.CameraMetaDataHelper.getEdgeModeNames;
import static com.sand5.videostabilize.hyperlapse.camera2.utils.CameraMetaDataHelper.getFocusDistanceCalibrationName;
import static com.sand5.videostabilize.hyperlapse.camera2.utils.CameraMetaDataHelper.getHardwareLevelName;
import static com.sand5.videostabilize.hyperlapse.camera2.utils.CameraMetaDataHelper.getNoiseReductionModeNames;
import static com.sand5.videostabilize.hyperlapse.camera2.utils.CameraMetaDataHelper.getScalarCroppingTypeName;
import static com.sand5.videostabilize.hyperlapse.camera2.utils.CameraMetaDataHelper.getTimestampSourceName;
import static com.sand5.videostabilize.hyperlapse.camera2.utils.CameraMetaDataHelper.getVideoStabilizationModeNames;
import static com.sand5.videostabilize.hyperlapse.camera2.utils.FOVCalculator.calculateFOV;
import static com.sand5.videostabilize.hyperlapse.camera2.utils.FOVCalculator.calculateFOV2;
import static com.sand5.videostabilize.hyperlapse.camera2.utils.TypeConversionHelpers.floatsToString;
import static com.sand5.videostabilize.hyperlapse.camera2.utils.TypeConversionHelpers.intsToString;

/**
 * Created by jeetdholakia on 1/11/17.
 */

public class CameraParameterLogging {

    private static final String CAMERAPARAMETERSTAG = "CAMERAPARAMETERSTAG";

    /**
     * Log all calibration data and save it so that openCV can use it for stabilization
     */
    public static void logAllCalibrationData(Context context, CameraCharacteristics characteristics, AutoFitTextureView mTextureView) {
        Log.d(TAG, "Logging all calibration data");

        // REQUEST HARDWARE LEVEL
        {
            int level = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            Log.d(CAMERAPARAMETERSTAG, "CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL: " + getHardwareLevelName(level));
        }

        StreamConfigurationMap map = characteristics
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Log.d(CAMERAPARAMETERSTAG, "SCALER_STREAM_CONFIGURATION_MAP: " + (map != null ? map.toString() : null));


        int orientation = context.getResources().getConfiguration().orientation;
        Log.d(CAMERAPARAMETERSTAG, "Orientation: " + orientation);

        Rect size = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        if (size != null) {
            Log.d(CAMERAPARAMETERSTAG, "SENSOR_INFO_ACTIVE_ARRAY_SIZE: "
                    + size.width() + "x" + size.height());
        } else {
            Log.d(CAMERAPARAMETERSTAG, "SENSOR_INFO_ACTIVE_ARRAY_SIZE: null");
        }

        int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        Log.d(CAMERAPARAMETERSTAG, "SENSOR_ORIENTATION: " + sensorOrientation);

        Log.d(CAMERAPARAMETERSTAG, "SENSOR_INFO_TIMESTAMP_SOURCE: " +
                getTimestampSourceName(characteristics.get(CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE)));

        Log.d(CAMERAPARAMETERSTAG, "LENS_INFO_FOCUS_DISTANCE_CALIBRATION: " +
                getFocusDistanceCalibrationName(characteristics.get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION)));

        Size size2 = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
        Log.d(CAMERAPARAMETERSTAG, "SENSOR_INFO_PIXEL_ARRAY_SIZE: " + size2.getWidth() + "x" + size2.getHeight());

        SizeF size3 = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
        Log.d(CAMERAPARAMETERSTAG, "SENSOR_INFO_PHYSICAL_SIZE: " + size3.getWidth() + "x" + size3.getHeight());

        int[] edgeModes = characteristics.get(CameraCharacteristics.EDGE_AVAILABLE_EDGE_MODES);

        if (edgeModes != null) {
            for (int i : edgeModes) {
                String name = getEdgeModeNames(i);
                Log.d(CAMERAPARAMETERSTAG, "EDGE_AVAILABLE_EDGE_MODES: " + name);
            }
        }


        Log.d(CAMERAPARAMETERSTAG, "CONTROL_AVAILABLE_VIDEO_STAB_MODES: "
                + intsToString(characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)));


        int[] noiseModes = characteristics.get(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES);

        if (noiseModes != null) {
            for (int i : noiseModes) {
                String name = getNoiseReductionModeNames(i);
                Log.d(CAMERAPARAMETERSTAG, "NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES: " + name);
            }
        }

        int[] availableVideoStabilizationModes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);

        if (availableVideoStabilizationModes != null) {
            for (int i : availableVideoStabilizationModes) {
                String name = getVideoStabilizationModeNames(i);
                Log.d(CAMERAPARAMETERSTAG, "CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES: " + name);
            }
        }

        Log.d(CAMERAPARAMETERSTAG, "LENS_INFO_AVAILABLE_APERTURES: "
                + Arrays.toString(characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)));

        Log.d(CAMERAPARAMETERSTAG, "LENS_INFO_AVAILABLE_FOCAL_LENGTHS: "
                + Arrays.toString(characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)));

        Log.d(CAMERAPARAMETERSTAG, "LENS_INFO_HYPERFOCAL_DISTANCE: "
                + characteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE));

        Log.d(CAMERAPARAMETERSTAG, "LENS_INFO_MINIMUM_FOCUS_DISTANCE: "
                + characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE));


        Log.d(CAMERAPARAMETERSTAG, "SCALER_CROPPING_TYPE: "
                + getScalarCroppingTypeName(characteristics.get(CameraCharacteristics.SCALER_CROPPING_TYPE)));

        Log.d(CAMERAPARAMETERSTAG, "SENSOR_REFERENCE_ILLUMINANT1: "
                + characteristics.get(CameraCharacteristics.SENSOR_REFERENCE_ILLUMINANT1));

        Log.d(CAMERAPARAMETERSTAG, "SENSOR_REFERENCE_ILLUMINANT2: "
                + characteristics.get(CameraCharacteristics.SENSOR_REFERENCE_ILLUMINANT2));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            float[] intrinsic = new float[5];
            float[] distortion = new float[4];

            intrinsic = characteristics.get(CameraCharacteristics.LENS_INTRINSIC_CALIBRATION);
            distortion = characteristics.get(CameraCharacteristics.LENS_RADIAL_DISTORTION);
            if (intrinsic != null)
                Log.d(CAMERAPARAMETERSTAG, "LENS_INTRINSIC_CALIBRATION: "
                        + Arrays.toString(intrinsic));
            else
                Log.d(CAMERAPARAMETERSTAG, "LENS_INTRINSIC_CALIBRATION: "
                        + "Null");
            if (distortion != null)
                Log.d(CAMERAPARAMETERSTAG, "LENS_RADIAL_DISTORTION: "
                        + Arrays.toString(distortion));
            else
                Log.d(CAMERAPARAMETERSTAG, "LENS_RADIAL_DISTORTION: "
                        + "Null");

            Log.d(CAMERAPARAMETERSTAG, "LENS_POSE_TRANSLATION: "
                    + floatsToString(characteristics.get(CameraCharacteristics.LENS_POSE_TRANSLATION)));

            Log.d(CAMERAPARAMETERSTAG, "LENS_RADIAL_DISTORTION: "
                    + floatsToString(characteristics.get(CameraCharacteristics.LENS_RADIAL_DISTORTION)));

            Rect preCorrectedSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE);
            if (preCorrectedSize != null) {
                Log.d(CAMERAPARAMETERSTAG, "SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE: "
                        + preCorrectedSize.width() + "x" + preCorrectedSize.height());
            } else {
                Log.d(CAMERAPARAMETERSTAG, "SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE: null");
            }
        }


        // SENSOR_INFO_EXPOSURE_TIME_RANGE
        {
            Range<Long> rr = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
            Log.d(CAMERAPARAMETERSTAG, "SENSOR_INFO_EXPOSURE_TIME_RANGE: " + rr);
        }

        // SENSOR_INFO_EXPOSURE_TIME_RANGE
        {
            Range<Integer> sr = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
            Log.d(CAMERAPARAMETERSTAG, "SENSOR_INFO_SENSITIVITY_RANGE: " + sr);
        }


        // CAPTURE REQUEST KEYS
        {
            String keys = "";
            for (CaptureRequest.Key key : characteristics.getAvailableCaptureRequestKeys()) {
                keys += key.getName() + "   ";
            }
            Log.d(CAMERAPARAMETERSTAG, "CameraCharacteristics.getAvailableCaptureRequestKeys() = " + keys);
        }

        // CAPTURE RESULT KEYS
        {
            String keys = "";
            for (CaptureResult.Key key : characteristics.getAvailableCaptureResultKeys()) {
                keys += key.getName() + "   ";
            }
            Log.d(CAMERAPARAMETERSTAG, "CameraCharacteristics.getAvailableCaptureResultKeys() = " + keys);
        }

        double fov = calculateFOV(characteristics);
        float[] fov2 = calculateFOV2(characteristics, mTextureView);
        Log.d(CAMERAPARAMETERSTAG, "Field of view: " + fov);
        Log.d(CAMERAPARAMETERSTAG, "Field of view2: " + Arrays.toString(fov2));

        /*int rotation = this.getRotationFromAccelerometer(this.accelerometerValues);
        Log.d(SENSORSTAG, "Rotation:" + rotation);
        Log.d(SENSORSTAG, "Orientation:" + getOrientation(rotation));*/

        // DeviceInformationUtils.printDisplayInfo(getActivity());
    }


    private void saveCalibrationData() {
        /*
        cam.getVerticalViewAngle
        cam.getHorizontalViewAngle
        Refer to Android application programming with opencv3 for getting a projection matrix
        For calibration visit the below link

        http://www.programcreek.com/java-api-examples/index.php?source_dir=Rubik-Cube-Wizard-master/Rubik%20Solver/src/org/ar/rubik/CameraCalibration.java
        Shows which features are available in which camera
        https://developer.android.com/reference/android/hardware/camera2/CameraDevice.html
         */


        /** Data to collect
         * Step 1 Capture Request:
         * Sensor orientation (DONE)
         * Phone/camera orientation (DONE)
         * Video stabilization modes - Crops the scalar region to keep the frames stabilized (DONE)
         * Transformation matrix
         * Aperture size (DONE)
         * Focal Length (DONE)
         * Focus Distance (DONE)
         * Hyperfocal distance (DONE)
         * Lens Intrinsic calibration (NULL)
         * Lens pose rotation (NULL)
         * Lens pose translation (NULL)
         * Sensor calibration transform1 (not MC)
         * Sensor calibration transform2 (not MC)
         * Sensor info active array size (DONE)
         * physical size (DONE)
         * sensor info timestamp source (DONE)
         * illuminant1 (not MC)
         * Scalar crop region (not MC)
         * Sensor Exposure Time (DONE)
         * Sensor Frame Duration (not MC)
         * Field of View (DONE)
         *
         * Step 2) (DONE)
         * Check <a href="https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics.html">All available camera params</a>
         */


    }

}
