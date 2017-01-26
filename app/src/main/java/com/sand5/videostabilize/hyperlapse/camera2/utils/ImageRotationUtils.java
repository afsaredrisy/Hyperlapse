package com.sand5.videostabilize.hyperlapse.camera2.utils;

import com.sand5.videostabilize.hyperlapse.camera2.beans.IntrinsicMatrix;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by jeetdholakia on 1/26/17.
 */

public class ImageRotationUtils {

    private float[] mTranslationMatrix = new float[16];
    private float[] mTransformMatrix = new float[16];
    private Mat src;
    private Mat output;


    public ImageRotationUtils() {
        mTranslationMatrix[0] = 1;
        mTranslationMatrix[3] = 1;
        mTranslationMatrix[7] = 1;
        mTranslationMatrix[5] = 1;
        mTranslationMatrix[10] = 1;
        //mTranslationMatrix[11] = f;
        mTranslationMatrix[15] = 1;
    }

    private void rotateImage(Mat src, float[] rotationMatrix, IntrinsicMatrix intrinsicMatrix) {

        Mat output = new Mat();
        Imgproc.warpPerspective(src, output, getTransformMatrix(src), new Size(src.width(), src.height()));

    }

    private void getAccumulatedRotation(Mat src, float[] gyroVectors, long timeStamp, int previous, int current, float focalLength, int gyroDelay, int gyroDrift) {
        /*
        Get A1 matrix
        transform = a1

         */


    }

    private Mat getTransformMatrix(Mat src) {

        /*
        a1 = 1,0,-w/2
             0,1,-h/2
             0,0,0
             0,0,1
         */

        //Multiply camera matrix, (translation matrix and rotation matrix) and a1 matrix

        return new Mat();
    }

}
