package com.sand5.videostabilize.hyperlapse.camera2.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orhanobut.logger.Logger;
import com.sand5.videostabilize.hyperlapse.R;
import com.sand5.videostabilize.hyperlapse.camera2.beans.AccelerometerData;
import com.sand5.videostabilize.hyperlapse.camera2.beans.FrameMat;
import com.sand5.videostabilize.hyperlapse.camera2.beans.GyroscopeData;
import com.sand5.videostabilize.hyperlapse.camera2.beans.IntrinsicMatrix;
import com.sand5.videostabilize.hyperlapse.camera2.beans.SmallRotationVectorData;
import com.sand5.videostabilize.hyperlapse.camera2.beans.SynchronizedFrameTimeStamp;
import com.sand5.videostabilize.hyperlapse.camera2.utils.ImageFramesDataStore;
import com.sand5.videostabilize.hyperlapse.camera2.utils.MatrixUtils;
import com.sand5.videostabilize.hyperlapse.camera2.utils.RotationVectorUtils;
import com.sand5.videostabilize.hyperlapse.camera2.utils.SynchronizedFrameTimeStampDataStore;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.ButterKnife;


public class RenderingFragment extends Fragment {

    ArrayList<float[]> rotationVectors = new ArrayList<>();
    float[][] rotationMatrix2DArray;
    float[][] transformationMatrix2DArray;
    private ArrayList<GyroscopeData> gyroscopeDataArrayList = new ArrayList<>();
    private MatOfPoint prevFeatures;
    private MatOfPoint nextFeatures;
    private MatOfPoint features;
    private MatOfByte status;
    private MatOfFloat err;
    private float[] transformationMatrixArray;
    private float[][] translationMatrix2DArray;
    private IntrinsicMatrix intrinsicMatrix;
    private float[] intrinsicMatrixArray = new float[12];
    private float[][] intrinsic2DArray = new float[3][4];

    public RenderingFragment() {
        // Required empty public constructor
    }


    public static RenderingFragment newInstance(String param1, String param2) {
        RenderingFragment fragment = new RenderingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rendering, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onFrameAvailable(FrameMat frameMat) {
        //Logger.d("Received a freaking mat!");
    }


    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onGyroscopeDataAvailable(GyroscopeData gyroscopeData) {
        Logger.d("Received gyro!");

    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onIntrinsicMatrix(IntrinsicMatrix intrinsicMatrix) {
        Logger.d("Received intrinsic matrix!");
        this.intrinsicMatrix = intrinsicMatrix;
        intrinsic2DArray = new float[][]{
                {intrinsicMatrix.getFocalLengthAngles()[0], intrinsicMatrix.getRollingShutterSkew(), intrinsicMatrix.getPrinciplePoints()[0], 0},
                {0, intrinsicMatrix.getFocalLengthAngles()[1], intrinsicMatrix.getPrinciplePoints()[1], 0},
                {0, 0, 1, 0}
        };

        translationMatrix2DArray = new float[][]{
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 0, intrinsicMatrix.getFocalLength()},
                {0, 0, 0, 1}
        };


    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onRotationVectorDataAvailable(SmallRotationVectorData smallRotationVectorData) {
        //Logger.d("Received a rotation!");
        //rotationVectorDataArrayList.add(0,rotationVectorData);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onAccelerometerDataAvailable(AccelerometerData accelerometerData) {
        Logger.d("Received accelerometer yippie ki yay!");

    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void startProcessing(String message) {
        //Logger.d("Start Processing");
        getRotationVectorForEachFrame();

    }

    private void getRotationVectorForEachFrame() {
        //Logger.d("Get Rotation Vector for each frame");
        ArrayList<Long> imageFrameTimeStamps = new ArrayList<>(ImageFramesDataStore.getAll().keySet());
        for (int i = 0; i < imageFrameTimeStamps.size(); i++) {
            Long timeStamp = imageFrameTimeStamps.get(i);
            rotationVectors.add(RotationVectorUtils.getEstimatedRotationVectors(timeStamp));
        }

        SynchronizedFrameTimeStampDataStore.printAll();
        //Logger.d("finished!");
        convertMatToRgbaMat();
    }

    private void convertMatToRgbaMat() {
        for (SynchronizedFrameTimeStamp synchronizedFrameTimeStamp : SynchronizedFrameTimeStampDataStore.getSynchronizedFrameTimeStampArrayList()) {
            Mat firstMat = synchronizedFrameTimeStamp.getMat();
            if (null == firstMat) {
                Logger.d("First mat null");
            } else {
                Logger.d("First mat not null");
            }
            Mat bgrMat = new Mat();
            Imgproc.cvtColor(firstMat, bgrMat, Imgproc.COLOR_YUV2BGR_I420);
            Mat rgbaMatOut = new Mat();
            Imgproc.cvtColor(bgrMat, rgbaMatOut, Imgproc.COLOR_BGR2RGBA, 0);
            synchronizedFrameTimeStamp.setMat(rgbaMatOut);
            float[] rotationMatrix = synchronizedFrameTimeStamp.getRotationMatrix();
            rotationMatrix2DArray = new float[][]{
                    {rotationMatrix[0], rotationMatrix[1], rotationMatrix[2], rotationMatrix[3]},
                    {rotationMatrix[4], rotationMatrix[5], rotationMatrix[6], rotationMatrix[7]},
                    {rotationMatrix[8], rotationMatrix[9], rotationMatrix[10], rotationMatrix[11]},
                    {rotationMatrix[12], rotationMatrix[13], rotationMatrix[14], rotationMatrix[15]}
            };
        }

        float[][] tempResult = MatrixUtils.multiplyMatrices(translationMatrix2DArray, rotationMatrix2DArray);
        transformationMatrix2DArray = MatrixUtils.multiplyMatrices(intrinsic2DArray, tempResult);
        transformationMatrixArray = MatrixUtils.get1DFrom2D(transformationMatrix2DArray);
        Logger.d(Arrays.deepToString(transformationMatrix2DArray));
        Logger.d(Arrays.toString(transformationMatrixArray));

        //useOpenCVSorcery();
    }

    private void useOpenCVSorcery() {
        Logger.d("Doing sorcery and shit");
        for (int i = 0; i < SynchronizedFrameTimeStampDataStore.getSynchronizedFrameTimeStampArrayList().size() - 1; i++) {
            if (i > 0) {
                Mat previousMat = SynchronizedFrameTimeStampDataStore.getSynchronizedFrameTimeStampArrayList().get(i).getMat();
                Mat currentMat = SynchronizedFrameTimeStampDataStore.getSynchronizedFrameTimeStampArrayList().get(i + 1).getMat();

                Mat oldGray = new Mat();
                Imgproc.cvtColor(previousMat, oldGray, Imgproc.COLOR_RGBA2GRAY);
                Mat newGray = new Mat();
                Imgproc.cvtColor(currentMat, newGray, Imgproc.COLOR_RGBA2GRAY);
                Imgproc.goodFeaturesToTrack(oldGray, prevFeatures, 10, 0.01, 10);

                MatOfPoint2f oldFeatures = new MatOfPoint2f();
                prevFeatures.convertTo(oldFeatures, CvType.CV_32FC2);
                //Imgproc.goodFeaturesToTrack(newGray,nextFeatures,10,0.01,10);

                MatOfPoint2f newFeatures = new MatOfPoint2f();
                //nextFeatures.convertTo(newFeatures, CvType.CV_32FC2);
                Video.calcOpticalFlowPyrLK(oldGray, newGray, oldFeatures, newFeatures, status, err, new Size(15, 15), 2, new TermCriteria(new double[]{TermCriteria.EPS, TermCriteria.COUNT}), 10, 0.03);

                Mat homography = new Mat();
                if (prevFeatures.toList().size() > 4) {
                    homography = Calib3d.findHomography(oldFeatures, newFeatures, Calib3d.RANSAC, 5.0);
                    Core.perspectiveTransform(oldFeatures, oldFeatures, homography);
                    Core.perspectiveTransform(newFeatures, newFeatures, homography);
                }
            } else {
                Mat currentMat = SynchronizedFrameTimeStampDataStore.getSynchronizedFrameTimeStampArrayList().get(i).getMat();
                Mat newGray = new Mat();
                Imgproc.cvtColor(currentMat, newGray, Imgproc.COLOR_RGBA2GRAY);
                err = new MatOfFloat();
                features = new MatOfPoint();
                prevFeatures = new MatOfPoint();
                nextFeatures = new MatOfPoint();
                status = new MatOfByte();
                int rowStep = 50, colStep = 100;
                int nRows = newGray.rows() / rowStep;
                int nCols = newGray.cols() / colStep;
                Point points[] = new Point[nRows * nCols];
                for (int j = 0; j < nRows; j++) {
                    for (int k = 0; k < nCols; k++) {
                        points[j * nCols + k] = new Point(j * colStep, j * rowStep);
                    }
                }
                features.fromArray(points);
                prevFeatures.fromList(features.toList());
            }
        }
        Logger.d("Perspective Transform DONE");

    }

    private void rotateImages() {

    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
