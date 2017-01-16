package com.sand5.videostabilize.hyperlapse.camera2.utils;

/**
 * Created by jeetdholakia on 1/12/17.
 */

public class VideoStabilizationLearningsDump {


    /**
     * Intrinsic parameters - Done!
     * Rotation Matrix - Done!
     * TODO : Extrinsic parameters
     * Time gap between frame and gyrotimestamp
     * Rolling shutter estimation (from the book nigga!)
     */


    /**
     * Stuff that is required for intrinsic parameters
     *
     * Intrinsic camera matrix is in the form of:
     * [ fx s x
     *   0 fy y
     *   0 0  1]
     * fx and fy are focal lengths in x and y direction
     * s is the skew
     * x and y are principle points
     * fx = x/tan(ax)
     * fy = y/tan(ay)
     *
     * Rolling shutter skew (DONE)
     * fx and fy(DONE)
     * Focal Length (DONE)
     * Time stamp (Frame,Gyro,Accelerometer,Rotation) (DONE)
     *
     * Lens Pose rotation
     * Inputs : x,y,z,w
     * Formula:
     * theta = 2* acos(w)
     * ax = x/ sin(theta/2)
     * ay = y/ sin(theta/2)
     * az = z/ sin(theta/2)
     *
     * To create a 3*3 rotation matrix
     * R = [ 1 - 2y^2 - 2z^2,       2xy - 2zw,       2xz + 2yw,
     *       2xy + 2zw, 1 - 2x^2 - 2z^2,       2yz - 2xw,
     *       2xz - 2yw,       2yz + 2xw, 1 - 2x^2 - 2y^2 ]
     *
     *
     * Formula to get angular velocity and angular position
     * http://faculty.ucmerced.edu/mhyang/papers/cvpr16_mobile_deblurring.pdf
     * https://developer.android.com/guide/topics/sensors/sensors_motion.html (Also includes getting gyro delta over a given time step)
     * Sensor fusion activity
     * Gyroscope and accelerometer use sensor coordinate system (use that or wrt world coordinate system?)
     * Seems gyro to calculate rotation, and accelerometer to calculate translation are main players here
     * For rotation wrt earths frame of reference,and remapping with sensors Frame of reference:
     * https://developer.android.com/guide/topics/sensors/sensors_motion.html
     *
     * Paper on auto calibration (camera - gyroscope fusion)
     * http://users.ece.utexas.edu/~bevans/papers/2015/autocalibration/autocalibrationIEEETransImageProcPaperDraft.pdf
     * http://users.ece.utexas.edu/~bevans/students/phd/chao_jia/phd.pdf
     * http://users.ece.utexas.edu/~bevans/projects/dsc/software/calibration/
     *
     *
     *
     *
     *
     * Extrinsic Parameters
     *
     *
     * Distortion Parameters
     *
     *
     *
     * Timestamp calibration is also an issue!
     * http://stackoverflow.com/questions/39745796/synchronizing-sensorevent-timestamp-with-system-nanotime-or-systemclock-e
     *
     *
     */


}
