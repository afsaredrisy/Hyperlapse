package com.sand5.videostabilize.hyperlapse.tests.cameracalibration;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.sand5.videostabilize.hyperlapse.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.List;

public class OpticalFlowTestActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OpticalFlowTestActivity";

    private static final int VIEW_MODE_KLT_TRACKER = 0;
    private static final int VIEW_MODE_OPTICAL_FLOW = 1;
    MatOfPoint2f prevFeatures, nextFeatures;
    MatOfPoint features;
    MatOfByte status;
    MatOfFloat err;
    private int mViewMode;
    private Mat mRgba;
    private Mat mIntermediateMat;
    private Mat mGray;
    private Mat mPrevGray;
    private MenuItem mItemPreviewKLT;
    private CameraBridgeViewBase mItemPreviewOpticalFlow, mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public OpticalFlowTestActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_optical_flow_test);
        mOpenCvCameraView = (CameraBridgeViewBase)
                findViewById(R.id.main_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);

    }


    private void stabilizeVideoAlgorithm() {
        /*
        while(success)
        read each frame;
        skip keypoints for first frame
        convert previous and current frame to grayscale
        old corners = cv2.goodFeaturesToTrack
        if(oldcorners =! null)
        use optical flow to identify where they are -> calcOpticalFlowPyrLK
        if(length of old corners>4)
        Do ransac homography
        end

        Handling rotations
        1)Use rodrigues function to generate rotation matrix R
        2)Generate translation matrix
        3)Generate cameraMatrix
        4) Generate A1 to rotate image about the center
        transform = cameraMatrix * (T*R * A1)
        4)Use warp perspective

        Accumulated rotations
        Use linear interpolation on gyro to bring them in sync
        calculate the rotation matrix using rodrigues and gryo signals
        Do z axis translation


        Calibration class
        

         */
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        resetVars();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d(TAG, "OnCameraFrame");
        final int viewMode = mViewMode;
        switch (viewMode) {
            case VIEW_MODE_OPTICAL_FLOW:
                mGray = inputFrame.gray();
                if (features.toArray().length == 0) {
                    int rowStep = 50, colStep = 100;
                    int nRows = mGray.rows() / rowStep, nCols = mGray.cols() / colStep;
                    Point points[] = new Point[nRows * nCols];
                    for (int i = 0; i < nRows; i++) {
                        for (int j = 0; j < nCols; j++) {
                            points[i * nCols + j] = new Point(j * colStep, i * rowStep);
                        }
                    }
                    features.fromArray(points);
                    prevFeatures.fromList(features.toList());
                    mPrevGray = mGray.clone();
                    break;
                }
                nextFeatures.fromArray(prevFeatures.toArray());
                Video.calcOpticalFlowPyrLK(mPrevGray, mGray,
                        prevFeatures, nextFeatures, status, err);
                List<Point> prevList = features.toList(),
                        nextList = nextFeatures.toList();
                Scalar color = new Scalar(255);
                for (int i = 0; i < prevList.size(); i++) {
                    Imgproc.line(mGray, prevList.get(i), nextList.get(i),
                            color);
                }
                mPrevGray = mGray.clone();
                break;
            default:
                mViewMode = VIEW_MODE_OPTICAL_FLOW;
        }
        return mGray;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.opticalflow, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.KLT:
                mViewMode = VIEW_MODE_KLT_TRACKER;
                resetVars();
                return true;
            case R.id.opticalFlow:
                mViewMode = VIEW_MODE_OPTICAL_FLOW;
                resetVars();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resetVars() {
        mPrevGray = new Mat(mGray.rows(), mGray.cols(), CvType.
                CV_8UC1);
        features = new MatOfPoint();
        prevFeatures = new MatOfPoint2f();
        nextFeatures = new MatOfPoint2f();
        status = new MatOfByte();
        err = new MatOfFloat();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

    }
}
