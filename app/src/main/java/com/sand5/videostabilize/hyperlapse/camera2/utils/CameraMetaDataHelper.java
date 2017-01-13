package com.sand5.videostabilize.hyperlapse.camera2.utils;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;

/**
 * Created by jeetdholakia on 1/11/17.
 */

public class CameraMetaDataHelper {

    public static String getVideoStabilizationModeNames(Integer level) {
        if (level == null) return "null";
        switch (level) {
            case CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF:
                return "CONTROL_VIDEO_STABILIZATION_MODE_OFF";
            case CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON:
                return "CONTROL_VIDEO_STABILIZATION_MODE_ON";
        }
        return "Unknown";
    }

    public static String getTimestampSourceName(Integer level) {
        if (level == null) return "null";
        switch (level) {
            case CameraMetadata.SENSOR_INFO_TIMESTAMP_SOURCE_REALTIME:
                return "SENSOR_INFO_TIMESTAMP_SOURCE_REALTIME";
            case CameraMetadata.SENSOR_INFO_TIMESTAMP_SOURCE_UNKNOWN:
                return "SENSOR_INFO_TIMESTAMP_SOURCE_UNKNOWN";
        }
        return "Unknown";
    }

    public static String getEdgeModeNames(Integer level) {
        if (level == null) return "null";
        switch (level) {
            case CameraMetadata.EDGE_MODE_OFF:
                return "EDGE_MODE_OFF";
            case CameraMetadata.EDGE_MODE_FAST:
                return "EDGE_MODE_FAST";
            case CameraMetadata.EDGE_MODE_HIGH_QUALITY:
                return "EDGE_MODE_HIGH_QUALITY";
            case CameraMetadata.EDGE_MODE_ZERO_SHUTTER_LAG:
                return "EDGE_MODE_ZERO_SHUTTER_LAG";
        }
        return "Unknown";
    }

    public static String getNoiseReductionModeNames(Integer level) {
        if (level == null) return "null";
        switch (level) {
            case CameraMetadata.NOISE_REDUCTION_MODE_FAST:
                return "NOISE_REDUCTION_MODE_FAST";
            case CameraMetadata.NOISE_REDUCTION_MODE_HIGH_QUALITY:
                return "NOISE_REDUCTION_MODE_HIGH_QUALITY";
            case CameraMetadata.NOISE_REDUCTION_MODE_MINIMAL:
                return "NOISE_REDUCTION_MODE_MINIMAL";
            case CameraMetadata.NOISE_REDUCTION_MODE_OFF:
                return "NOISE_REDUCTION_MODE_OFF";
            case CameraMetadata.NOISE_REDUCTION_MODE_ZERO_SHUTTER_LAG:
                return "NOISE_REDUCTION_MODE_ZERO_SHUTTER_LAG";
        }
        return "Unknown";
    }

    public static String getFocusDistanceCalibrationName(Integer level) {
        if (level == null) return "null";
        switch (level) {
            case CameraMetadata.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_APPROXIMATE:
                return "LENS_INFO_FOCUS_DISTANCE_CALIBRATION_APPROXIMATE";
            case CameraMetadata.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_CALIBRATED:
                return "LENS_INFO_FOCUS_DISTANCE_CALIBRATION_CALIBRATED";
            case CameraMetadata.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_UNCALIBRATED:
                return "LENS_INFO_FOCUS_DISTANCE_CALIBRATION_UNCALIBRATED";
        }
        return "Unknown";
    }

    public static String getScalarCroppingTypeName(Integer level) {
        if (level == null) return "null";
        switch (level) {
            case CameraMetadata.SCALER_CROPPING_TYPE_CENTER_ONLY:
                return "SCALER_CROPPING_TYPE_CENTER_ONLY";
            case CameraMetadata.SCALER_CROPPING_TYPE_FREEFORM:
                return "SCALER_CROPPING_TYPE_CENTER_ONLY";
        }
        return "Unknown";
    }

    public static String getCapabilityName(int format) {
        switch (format) {
            case CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE:
                return "REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE";
            case CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR:
                return "REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR";
            case CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING:
                return "REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING";
            case CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW:
                return "REQUEST_AVAILABLE_CAPABILITIES_RAW";
            case 4:
                return "REQUEST_AVAILABLE_CAPABILITIES_OPAQUE_REPROCESSING";
            case 5:
                return "REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS";
            case 6:
                return "REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE";
            case 7:
                return "REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING";
            case 8:
                return "REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT";
        }
        return "Unknown";
    }

    public static String getHardwareLevelName(int level) {
        switch (level) {
            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                return "INFO_SUPPORTED_HARDWARE_LEVEL_FULL";
            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                return "INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED";
            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                return "INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY";
        }
        return "Unknown";
    }
}
