package com.sand5.videostabilize.hyperlapse.camera2.opencvutils;

/**
 * Created by jeetdholakia on 1/11/17.
 */

public class OpenCVUtils {

    /**
     * Get OpenCV Camera Matrix
     *
     * This matrix represents the intrinsic properties of the camera.
     * Matrix is basically:
     *
     *    |   Fx    0    Cx  |
     *    |    0   Fy    Cy  |
     *    |    0    0     1  |
     *
     *    Fx := X Focal Length
     *    Fy := Y Focal Length
     *    Cx := X Optical Center
     *    Cy := Y Optical Center
     *
     *
     * @return
     */
    /*public Mat getOpenCVCameraMatrix(int width, int height) {

        Log.v(TAG_CAL, "CameraCalibration.getOpenCVMatrix(): width=" + width + " height=" + height);

        double focalLengthXPixels = width / ( 2.0 * Math.tan(0.5 * fovX));
        double focalLengthYPixels = height / ( 2.0 * Math.tan(0.5 * fovY));

        Mat cameraMatrix          = new Mat(3, 3, CvType.CV_64FC1);
        cameraMatrix.put(0, 0, focalLengthXPixels);   // should be X focal length in pixels.
        cameraMatrix.put(0, 1, 0.0);
        cameraMatrix.put(0, 2, width/2.0);
        cameraMatrix.put(1, 0, 0.0);
        cameraMatrix.put(1, 1, focalLengthYPixels);  // should be Y focal length in pixels.
        cameraMatrix.put(1, 2, height/2.0);
        cameraMatrix.put(2, 0, 0.0);
        cameraMatrix.put(2, 1, 0.0);
        cameraMatrix.put(2, 2, 1.0);

        Log.v(TAG_CAL, "Android Camera Calibration Matrix: ");
        Log.v(TAG_CAL, cameraMatrix.dump());

//     =+= From Android Camera Calibration App at resolution 1920 x 1080
//     cameraMatrix.put(0, 0, 1686.1);
//     cameraMatrix.put(0, 1, 0.0);
//     cameraMatrix.put(0, 2, 959.5);
//     cameraMatrix.put(1, 0, 0.0);
//     cameraMatrix.put(1, 1, 1686.1);
//     cameraMatrix.put(1, 2, 539.5);
//     cameraMatrix.put(2, 0, 0.0);
//     cameraMatrix.put(2, 1, 0.0);
//     cameraMatrix.put(2, 2, 1.0);
//
//     Log.v(Constants.TAG_CAL, "Camera Calibration App Matrix: ");
//     Log.v(Constants.TAG_CAL, cameraMatrix.dump());

        return cameraMatrix;
    }


    /**
     * Return Camera Calibration Coefficients: specifically k1, k1 [, p3, p4 [, k3]] as defined by OpenCV
     *
     * @return

    public double[] getDistortionCoefficients() {

//     =+= From Android Camera Calibration App at resolution 1920 x 1080
//  double [] distCoeff =  {
//    0.0940951391875556,
//    0.856988256473992,
//    0,
//    0,
//    -4.559694183079539};
//
//  return distCoeff;

        // No distortion coefficients used.
        return null;

    }*/

}
