package com.sand5.videostabilize.hyperlapse.tests.cameracalibration;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class CalibrationResult {
    private static final String TAG = "OCVSample::CalibrationResult";

    private static final int CAMERA_MATRIX_ROWS = 3;
    private static final int CAMERA_MATRIX_COLS = 3;
    private static final int DISTORTION_COEFFICIENTS_SIZE = 5;

    public static void save(Activity activity, Mat cameraMatrix, Mat distortionCoefficients) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        JSONObject mainObject = new JSONObject();
        JSONArray cameraMatrixObject = new JSONArray();
        JSONArray distortionMatrixObject = new JSONArray();
        double[] cameraMatrixArray = new double[CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS];
        cameraMatrix.get(0, 0, cameraMatrixArray);
        for (int i = 0; i < CAMERA_MATRIX_ROWS; i++) {
            for (int j = 0; j < CAMERA_MATRIX_COLS; j++) {
                Integer id = i * CAMERA_MATRIX_ROWS + j;
                editor.putFloat(id.toString(), (float) cameraMatrixArray[id]);
                try {
                    //cameraMatrixObject.put(id.toString(),(float)cameraMatrixArray[id]);
                    cameraMatrixObject.put(cameraMatrixArray[id]);
                    mainObject.put("Camera Matrix", cameraMatrixObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        double[] distortionCoefficientsArray = new double[DISTORTION_COEFFICIENTS_SIZE];
        distortionCoefficients.get(0, 0, distortionCoefficientsArray);

        int shift = CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS;
        for (Integer i = shift; i < DISTORTION_COEFFICIENTS_SIZE + shift; i++) {
            editor.putFloat(i.toString(), (float) distortionCoefficientsArray[i - shift]);
            try {
                distortionMatrixObject.put(distortionCoefficientsArray[i - shift]);
                mainObject.put("Distortion Matrix", cameraMatrixObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        editor.apply();
        try {
            String fileName = DateFormat.format("MM-dd-yyyyy-h-mmssaa", System.currentTimeMillis()).toString();
            // this will create a new name everytime and unique
            File root = new File(Environment.getExternalStorageDirectory(), "CalibrationData");
            // if external memory exists and folder with name Notes
            if (!root.exists()) {
                root.mkdirs(); // this will create folder.
            }
            File filepath = new File(root, fileName + ".txt");  // file path to save
            FileWriter writer = new FileWriter(filepath);
            writer.append(mainObject.toString());
            writer.flush();
            writer.close();
            Log.d(TAG, "File saved");

        } catch (IOException e) {
            e.printStackTrace();

        }
        Log.i(TAG, "Saved camera matrix: " + cameraMatrix.dump());
        Log.i(TAG, "Saved distortion coefficients: " + distortionCoefficients.dump());
    }

    public static boolean tryLoad(Activity activity, Mat cameraMatrix, Mat distortionCoefficients) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        if (sharedPref.getFloat("0", -1) == -1) {
            Log.i(TAG, "No previous calibration results found");
            return false;
        }

        double[] cameraMatrixArray = new double[CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS];
        for (int i = 0; i < CAMERA_MATRIX_ROWS; i++) {
            for (int j = 0; j < CAMERA_MATRIX_COLS; j++) {
                Integer id = i * CAMERA_MATRIX_ROWS + j;
                cameraMatrixArray[id] = sharedPref.getFloat(id.toString(), -1);
            }
        }
        cameraMatrix.put(0, 0, cameraMatrixArray);
        Log.i(TAG, "Loaded camera matrix: " + cameraMatrix.dump());

        double[] distortionCoefficientsArray = new double[DISTORTION_COEFFICIENTS_SIZE];
        int shift = CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS;
        for (Integer i = shift; i < DISTORTION_COEFFICIENTS_SIZE + shift; i++) {
            distortionCoefficientsArray[i - shift] = sharedPref.getFloat(i.toString(), -1);
        }
        distortionCoefficients.put(0, 0, distortionCoefficientsArray);
        Log.i(TAG, "Loaded distortion coefficients: " + distortionCoefficients.dump());

        return true;
    }
}
