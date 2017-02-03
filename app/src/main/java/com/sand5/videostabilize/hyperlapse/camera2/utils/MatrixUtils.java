package com.sand5.videostabilize.hyperlapse.camera2.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeetdholakia on 2/2/17.
 */

public class MatrixUtils {

    public static float[][] multiplyMatrices(float[][] matrix1, float[][] matrix2) {
        float[][] result = new float[matrix1.length][matrix2[0].length];
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix2[0].length; j++) {
                for (int k = 0; k < matrix1[0].length; k++) {
                    result[i][j] += matrix1[i][k] * matrix2[k][j];
                }
            }
        }
        return result;
    }

    public static float[] get1DFrom2D(float[][] arr) {
        List<Float> list = new ArrayList<Float>();
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                list.add(arr[i][j]);
            }
        }
        float[] vector = new float[list.size()];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = list.get(i);
        }
        return vector;
    }
}
