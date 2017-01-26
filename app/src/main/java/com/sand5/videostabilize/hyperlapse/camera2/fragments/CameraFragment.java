package com.sand5.videostabilize.hyperlapse.camera2.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.sand5.videostabilize.hyperlapse.R;
import com.sand5.videostabilize.hyperlapse.camera2.activities.VideoDecoderActivity;
import com.sand5.videostabilize.hyperlapse.camera2.beans.AccelerometerData;
import com.sand5.videostabilize.hyperlapse.camera2.beans.FrameMat;
import com.sand5.videostabilize.hyperlapse.camera2.beans.FrameTimeStampData;
import com.sand5.videostabilize.hyperlapse.camera2.beans.GyroscopeData;
import com.sand5.videostabilize.hyperlapse.camera2.beans.IntrinsicMatrix;
import com.sand5.videostabilize.hyperlapse.camera2.beans.RotationVectorData;
import com.sand5.videostabilize.hyperlapse.camera2.beans.SmallRotationVectorData;
import com.sand5.videostabilize.hyperlapse.camera2.utils.AutoFitTextureView;
import com.sand5.videostabilize.hyperlapse.camera2.utils.CameraMetaDataHelper;
import com.sand5.videostabilize.hyperlapse.camera2.utils.CameraSizeUtils;
import com.sand5.videostabilize.hyperlapse.camera2.utils.ErrorDialog;
import com.sand5.videostabilize.hyperlapse.camera2.utils.FOVCalculator;
import com.sand5.videostabilize.hyperlapse.camera2.utils.ImageFramesDataStore;
import com.sand5.videostabilize.hyperlapse.camera2.utils.ImageUtils;
import com.sand5.videostabilize.hyperlapse.camera2.utils.RotationVectorDataStore;

import org.greenrobot.eventbus.EventBus;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.id.list;
import static com.sand5.videostabilize.hyperlapse.camera2.utils.CameraParameterLogging.logAllCalibrationData;
import static com.sand5.videostabilize.hyperlapse.camera2.utils.FileUtils.getVideoFilePath;
import static com.sand5.videostabilize.hyperlapse.tests.SensorFusionActivity.EPSILON;

public class CameraFragment extends Fragment
        implements View.OnClickListener, FragmentCompat.OnRequestPermissionsResultCallback,
        SensorEventListener {

    // TODO: 1/13/17 Start by checking hardware device level for camera parameters and sensors
    // TODO: 1/13/17 Add gravity, geo-magnet and linear acceleration sensors

    //permissions
    public static final int REQUEST_VIDEO_PERMISSIONS = 1;
    public static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};
    //tags
    private static final String TAG = "Camera2VideoFragment";
    private static final String SENSORSTAG = "SensorsParameters";
    private static final String CAPTURELOGTAG = "CaptureParameters";
    private static final String SENSORTIMESTAMPLOG = "SensorTimeStampLog";
    private static final String FRAMETIMESTAPLOGTAG = "FrameTimeStampLog";
    //orientation variablies
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private static final String FRAGMENT_DIALOG = "dialog";
    private static final float NS2S = 1.0f / 1000000000.0f;

    //orientation constants
    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private final float[] mRotationMatrix = new float[16];
    private final float[] deltaRotationVector = new float[4];
    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    @BindView(R.id.texture)
    AutoFitTextureView mTextureView;
    ImageReader.OnImageAvailableListener mImageAvailable = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (null == image) {
                return;
            } else {
                Long systemTime = System.nanoTime();
                Mat mat = ImageUtils.imageToMat(image);
                FrameMat frameMat = new FrameMat(systemTime, mat);
                Log.d("IMAGEAVAILABLE:", "TimeStamp:" + systemTime);
                ImageFramesDataStore.add(frameMat);
                //EventBus.getDefault().post(frameMat);


                // LinkedHashMap<Long,Mat> frameMatLinkedHashMap = new LinkedHashMap<>();
                //frameMatLinkedHashMap.put(systemTime,mat);
                /*Mat bgrMat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4);
                Imgproc.cvtColor(mat, bgrMat, Imgproc.COLOR_YUV2BGR_I420);
                Mat rgbaMatOut = new Mat();
                Imgproc.cvtColor(bgrMat, rgbaMatOut, Imgproc.COLOR_BGR2RGBA, 0);
                FrameMat frameMat = new FrameMat(image.getTimestamp(),rgbaMatOut);
                */
                /*if (imageArrayList.size() < 90) {
                    imageArrayList.add(0, bgrMat);
                }*/
            }
            image.close();
        }
    };
    private Button mButtonVideo;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mPreviewSession;
    /**
     * The {@link Size} of camera preview.
     */
    private Size mPreviewSize;
    /**
     * The {@link Size} of video recording.
     */
    private Size mVideoSize;
    private MediaRecorder mMediaRecorder;
    private boolean mIsRecordingVideo;
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;
    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;
    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private int sensorOrientation;
    private Integer mSensorOrientation;
    private String videoFilePath;
    private CaptureRequest.Builder mPreviewBuilder;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor rotation;
    private ArrayList<FrameTimeStampData> frameTimeStampDataArrayList = new ArrayList<>();
    private ArrayList<GyroscopeData> gyroscopeDataArrayList = new ArrayList<>();
    private ArrayList<RotationVectorData> rotationVectorDataArrayList = new ArrayList<>();
    private ArrayList<AccelerometerData> accelerometerDataArrayList = new ArrayList<>();
    private float focalLength = 0;
    private long rollingShutterSkew = 0L;
    private String hardwareLevel;
    private IntrinsicMatrix intrinsicMatrix;
    private float[] focalLengthAngles;
    private float focusDistance;
    private float principlePoints[];
    private Rect activeRect;
    private int rectangleWidth;
    private int rectangleHeight;
    private ImageReader mImageReader;
    private ArrayList<FrameMat> imageArrayList = new ArrayList<>();
    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its status.
     */
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            mCameraOpenCloseLock.release();
            if (null != mTextureView) {
                configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };
    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

    };
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.d(TAG, "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_camera2_video, container, false);
        ButterKnife.bind(this, mView);
        return mView;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mButtonVideo = (Button) view.findViewById(R.id.video);
        mButtonVideo.setOnClickListener(this);
        initSensors();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerSensors();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, getActivity(), mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video: {
                if (mIsRecordingVideo) {
                    stopRecordingVideo();
                } else {
                    startRecordingVideo();
                }
                break;
            }
        }
    }

    /**
     * Tries to open a {@link CameraDevice}. The result is listened by `mStateCallback`.
     */
    private void openCamera(int width, int height) {

        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            requestVideoPermissions();
            return;
        }
        final Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            Log.d(TAG, "tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            hardwareLevel = CameraMetaDataHelper.getHardwareLevelName(characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL));
            // Choose the sizes for camera preview and video recording
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            logAllCalibrationData(getActivity(), characteristics, mTextureView);

            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            sensorOrientation = mSensorOrientation;
            focalLengthAngles = FOVCalculator.calculateFOV2(characteristics, mTextureView);

            assert map != null;
            mVideoSize = CameraSizeUtils.chooseVideoSize(map.getOutputSizes(MediaRecorder.class));

            principlePoints = new float[2];
            principlePoints[0] = mVideoSize.getWidth();
            principlePoints[1] = mVideoSize.getHeight();

            mImageReader = ImageReader.newInstance(mVideoSize.getWidth(),
                    mVideoSize.getHeight(),
                    ImageFormat.YUV_420_888, 3);

            mImageReader.setOnImageAvailableListener(mImageAvailable, mBackgroundHandler);

            mPreviewSize = CameraSizeUtils.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    width, height, mVideoSize);

            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }

            configureTransform(width, height);
            mMediaRecorder = new MediaRecorder();

            //Permission checker
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestVideoPermissions();
                return;
            }
            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            Toast.makeText(activity, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
            activity.finish();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Start the camera preview.
     */
    private void startPreview() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            List surfaces = new ArrayList<>();

            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Activity activity = getActivity();
                    if (null != activity) {
                        Toast.makeText(activity, getResources().getString(R.string.error_configure_failed), Toast.LENGTH_SHORT).show();
                    }
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreviewForRecording() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                    Log.d(CAPTURELOGTAG, "OnCaptureStarted");
                }

                @Override
                public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
                    super.onCaptureProgressed(session, request, partialResult);
                    Log.d(CAPTURELOGTAG, "onCaptureProgressed");
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, @NonNull CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    rollingShutterSkew = result.get(CaptureResult.SENSOR_ROLLING_SHUTTER_SKEW);
                    focalLength = result.get(CaptureResult.LENS_FOCAL_LENGTH);
                    focusDistance = result.get(CaptureResult.LENS_FOCUS_DISTANCE);
                    Log.d(CAPTURELOGTAG, "onCaptureCompleted");

                    /*
                    Log.d(CAPTURELOGTAG, "Rolling shutter skew: " + result.get(CaptureResult.SENSOR_ROLLING_SHUTTER_SKEW));
                    Log.d(CAPTURELOGTAG, "Focal Length of lens: " + result.get(CaptureResult.LENS_FOCAL_LENGTH));


                    long nano = System.nanoTime();
                    long elapsedNanos = SystemClock.elapsedRealtimeNanos();
                    long systemCurrentMillis = System.currentTimeMillis();
                    long frameTimeStamp = result.get(CaptureResult.SENSOR_TIMESTAMP);

                    FrameTimeStampData data = new FrameTimeStampData(systemCurrentMillis,nano,elapsedNanos,frameTimeStamp);
                    frameTimeStampDataArrayList.add(data);*/

                    /*frameTimeStampDelta.add(0, frameTimeStamp);
                    String frameStamp = "SystemCurrentTimeMillis: " + systemCurrentMillis + "\n" +
                            "SystemNano: " + nano + "\n" +
                            "System ElapsedRTNanos: " + elapsedNanos + "\n" +
                            "Frame Timestamp: " + frameTimeStamp + "\n" +
                            "SystemNano - frame delta: " + (nano - frameTimeStamp) + "\n" +
                            "SystemElapsed - frame delta: " + (elapsedNanos - frameTimeStamp) + "\n";
                    frameTimeStamps.put(frameStamp);
                    //Log.d(FRAMETIMESTAPLOGTAG, frameStamp);
                    Log.d(CAPTURELOGTAG, "Focal length: " + result.get(CaptureResult.LENS_FOCAL_LENGTH));
                    Log.d(CAPTURELOGTAG, "Frame Number: " + result.getFrameNumber());


                    //result.getKeys();
                    //Log.d(CAPTURELOGTAG,"Result keys:" + result.getKeys().toString());
                    List<CaptureResult> partialResult = result.getPartialResults();
                    for(CaptureResult result1 : partialResult){
                       // result1.getKeys();
                        Long rollingShuterSkew = result1.get(CaptureResult.SENSOR_ROLLING_SHUTTER_SKEW);
                        result1.getFrameNumber();
                    }*/
                }

                @Override
                public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                    super.onCaptureFailed(session, request, failure);
                    Log.d(CAPTURELOGTAG, "onCaptureFailed");
                }

                @Override
                public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId, long frameNumber) {
                    super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
                    Log.d(CAPTURELOGTAG, "onCaptureSequenceCompleted");
                    intrinsicMatrix = new IntrinsicMatrix(rollingShutterSkew, focalLength, focusDistance, focalLengthAngles, principlePoints);
                    //saveVideoProcessingData();
                    printTimeStampData();
                    //calculateTimeStep();

                    String message = "Stopped";
                    EventBus.getDefault().post(intrinsicMatrix);
                    EventBus.getDefault().post(message);

                }

                @Override
                public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId) {
                    super.onCaptureSequenceAborted(session, sequenceId);
                    Log.d(CAPTURELOGTAG, "onCaptureSequenceAborted");
                }

                @Override
                public void onCaptureBufferLost(CameraCaptureSession session, CaptureRequest request, Surface target, long frameNumber) {
                    super.onCaptureBufferLost(session, request, target, frameNumber);
                    Log.d(CAPTURELOGTAG, "onCaptureBufferLost");

                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initial data that is required
     * Gyroscope time stamp and rotations on all axis (DONE)
     * Rotation vectors with the time stamps (DONE)
     * Accelerometer data
     * Frame time stamps
     * Focal Length of lens (DONE) by obtaining FOV)
     * Delay between gyroscope and frame timestamps
     * Bias in gyroscope (TODO)
     * Duration of rolling shutter (DONE)
     */
    private void saveVideoProcessingData() {
        //Send gyroscope data
        //EventBus.getDefault().postSticky(gyroscopeDataArrayList);
        //Send accelerometer data
        //EventBus.getDefault().postSticky(accelerometerDataArrayList);
        //Send rotationVector data
        //EventBus.getDefault().postSticky(rotationVectorDataArrayList);
        //Send intrinsic matrix data
        EventBus.getDefault().postSticky(intrinsicMatrix);
        ArrayList<ArrayList> bundleList = new ArrayList<>();
        bundleList.add(gyroscopeDataArrayList);
        bundleList.add(accelerometerDataArrayList);
        bundleList.add(rotationVectorDataArrayList);
        bundleList.add(imageArrayList);
        EventBus.getDefault().postSticky(bundleList);
        //Start postprocessing activity
        Intent postPostprocessing = new Intent(getActivity(), VideoDecoderActivity.class);
        startActivity(postPostprocessing);
    }

    private void saveArrayListToFile() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            try {
                // create a file in downloads directory
                FileOutputStream fos =
                        new FileOutputStream(
                                new File(Environment.getExternalStorageDirectory(), "name.ser")
                        );
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(list);
                os.close();
                Log.v("MyApp", "File has been written");
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.v("MyApp", "File didn't write");
            }
        }
    }

    private void calculateTimeStep() {
        for (int i = 0; i < frameTimeStampDataArrayList.size() - 1; i++) {
            long currentFrameTimeStamp = frameTimeStampDataArrayList.get(i + 1).getFrameTimeStamp();
            long nextFrameTimeStamp = frameTimeStampDataArrayList.get(i).getFrameTimeStamp();
            long timeStamp = currentFrameTimeStamp - nextFrameTimeStamp;
            Log.d(CAPTURELOGTAG, "TIME STEP:" + timeStamp);
        }

        /*for(int i=0;i<rotationVectorDataArrayList.size();i++){
            Log.d(CAPTURELOGTAG, "Rotation Vectors:" + Arrays.toString(rotationVectorDataArrayList.get(i).getRotationVectorEvent()));
        }*/
    }

    private void printTimeStampData() {

        /*File myfile = new File(Environment.getExternalStorageDirectory(),"Hyperlapse Timestamps");
        try {

            if (!myfile.exists()) {
                myfile.mkdirs();
            }
            if(myfile.exists() || myfile.createNewFile()){
                FileOutputStream fos = new FileOutputStream(myfile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(gyroscopeTimeStamps);
                oos.writeObject(frameTimeStamps);
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        /*File root = new File(Environment.getExternalStorageDirectory(), "Hyperlapse Timestamps");
        if (!root.exists()) {
            root.mkdirs();
        }
        try {
            String fileName = "GyroscopeTimeStamps.txt";
            File gpxfile = new File(root, fileName);
            FileWriter file = new FileWriter(gpxfile);
            file.write(gyroscopeTimeStamps.toString());
            file.flush();
            file.close();
            Toast.makeText(getActivity(), "Gyro Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("TAG", "Error in Writing: " + e.getLocalizedMessage());
        }

        try {

            String fileName = "FrameTimeStamps.txt";
            File gpxfile = new File(root, fileName);
            FileWriter file = new FileWriter(gpxfile);
            file.write(frameTimeStamps.toString());
            file.flush();
            file.close();
            Toast.makeText(getActivity(), "Frame Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("TAG", "Error in Writing: " + e.getLocalizedMessage());
        }*/

        /*try {
            File root = new File(Environment.getExternalStorageDirectory(), "Hyperlapse Timestamps");
            if (!root.exists()) {
                root.mkdirs();
            }
            String fileName = String.valueOf(System.currentTimeMillis()) + ".txt";
            File gpxfile = new File(root, fileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    private void startRecordingVideo() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            //setUpMediaRecorder();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();
            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);
            // Set up Surface for the MediaRecorder
            /*Surface mRecorderSurface = mMediaRecorder.getSurface();
            surfaces.add(mRecorderSurface);
            mPreviewBuilder.addTarget(mRecorderSurface);*/

            Surface readerSurface = mImageReader.getSurface();
            surfaces.add(readerSurface);
            mPreviewBuilder.addTarget(readerSurface);
            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreviewForRecording();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // UI
                            mButtonVideo.setText(R.string.stop);
                            mIsRecordingVideo = true;

                            // Start recording
                            // mMediaRecorder.start();
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Activity activity = getActivity();
                    if (null != activity) {
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Configures the necessary {@link Matrix} transformation to `mTextureView`.
     * This method should not to be called until the camera preview size is determined in
     * openCamera, or until the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private void setUpMediaRecorder() throws IOException {
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(getVideoFilePath(getActivity()));
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        mMediaRecorder.prepare();
    }

    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    private void stopRecordingVideo() {
        // UI
        mIsRecordingVideo = false;
        mButtonVideo.setText(R.string.record);
        // Stop recording
        //mMediaRecorder.stop();
        //mMediaRecorder.reset();

        Activity activity = getActivity();
        if (null != activity) {
            Snackbar.make(getView(), "Video Saved", Snackbar.LENGTH_LONG).show();
        }
        videoFilePath = null;
        startPreview();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (this.accelerometer != null && event.sensor.equals(this.accelerometer)) {
            /*float[] accelerometerValues = event.values;
            long accelerometerTimeStamp = event.timestamp;
             Log.d(SENSORSTAG, "Accelerometer Timestamp:" + event.timestamp);
            logSensorValues("Accelerometer", event);*/
        } else if (this.gyroscope != null && event.sensor.equals(this.gyroscope)) {
            /*float[] gyroscopeValues = event.values;
            long gyroscopeTimeStamp = event.timestamp;
            //Current time
            long systemCurrentTimeMillis = System.currentTimeMillis();
            //Most precise timestamp, with 0 value being when device was last rebooted, used to measure delta with another timestamp on same device.
            long nanoTime = System.nanoTime();
            //Nanoseconds since boot
            long elapsedRealTimeNanos = SystemClock.elapsedRealtimeNanos();
            if (mIsRecordingVideo) {
                //GyroscopeData data = new GyroscopeData(systemCurrentTimeMillis, nanoTime, elapsedRealTimeNanos, gyroscopeTimeStamp, gyroscopeValues);
                //gyroscopeDataArrayList.add(data);
                //EventBus.getDefault().post(data);
                // getRotationChangeOverTime(event);

                String sampleGyro = "X: " + gyroscopeValues[0] + "\n" +
                        "Y: " + gyroscopeValues[1] + "\n" +
                        "Z: " + gyroscopeValues[2] + "\n" +
                        "Timestamp: " + gyroscopeTimeStamp + "\n";
                //  Log.d(SENSORTIMESTAMPLOG,"Gyroscope Sample:" + sampleGyro);

            }

            /*String timeStampData = "SystemCurrentTimeMillis: " + systemCurrentTimeMillis + "\n" +
                    "SystemNano: " + nanoTime + "\n" +
                    "SystemElapsedRTNanos: " + elapsedRealTimeNanos + "\n" +
                    "Gyroscope: " + gyroscopeTimeStamp + "\n" +
                    "SystemNano - Gyroscope delta: " + (nanoTime - gyroscopeTimeStamp) + "\n" +
                    "SystemElapsed - Gyroscope delta: " + (elapsedRealTimeNanos - gyroscopeTimeStamp) + "\n";
            //logTimeStampDataOnFile(getActivity(),timeStampData);
            if (mIsRecordingVideo)
                gyroscopeTimeStamps.put(timeStampData);
            //Log.d(SENSORTIMESTAMPLOG, timeStampData);
            Log.d(SENSORSTAG, "Gyroscope Timestamp:" + event.timestamp);
            logSensorValues("Gyroscope", event);*/


        } else if (this.rotation != null && event.sensor.equals(this.rotation)) {
            float[] rotationValues = event.values;
            long rotationTimeStamp = event.timestamp;
            //Current time
            //long systemCurrentTimeMillis = System.currentTimeMillis();
            //Most precise timestamp, with 0 value being when device was last rebooted, used to measure delta with another timestamp on same device.
            // long nanoTime = System.nanoTime();
            //Nanoseconds since boot
            //long elapsedRealTimeNanos = SystemClock.elapsedRealtimeNanos();
            // Log.d(SENSORSTAG, "Rotation Timestamp:" + event.timestamp);
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, event.values);
            if (mIsRecordingVideo) {
                LinkedHashMap<Long, float[]> smallRotationVectorDataLinkedHashMap = new LinkedHashMap<>();
                Long timestamp = System.nanoTime();
                SmallRotationVectorData smallData = new SmallRotationVectorData(timestamp, mRotationMatrix, event.values);
                RotationVectorDataStore.add(smallData);
                //smallRotationVectorDataLinkedHashMap.put(timestamp,mRotationMatrix);
                //EventBus.getDefault().post(smallData);
                //RotationVectorData data = new RotationVectorData(systemCurrentTimeMillis, nanoTime, elapsedRealTimeNanos, rotationTimeStamp, rotationValues, mRotationMatrix);
                //rotationVectorDataArrayList.add(data);

              /* String sampleRotation = "X: " + rotationValues[0] + "\n" +
                       "Y: " + rotationValues[1] + "\n" +
                       "Z: " + rotationValues[2] + "\n" +
                       "Timestamp: " + timestamp + "\n" +
                       "Rotation Matrix" + Arrays.toString(mRotationMatrix);
               Log.d(SENSORTIMESTAMPLOG,"Rotation Sample:" + sampleRotation);*/
            }

        }
    }


    private void getRotationChangeOverTime(ArrayList<RotationVectorData> rotationVectorDataArrayList) {
        for (int i = 0; i < rotationVectorDataArrayList.size() - 1; i++) {
            RotationVectorData oldData = rotationVectorDataArrayList.get(i);
            RotationVectorData newData = rotationVectorDataArrayList.get(i + 1);
            float[] rotationCurrent = oldData.getRotationRawEvent();
            long oldTimeStamp = oldData.getRotationTimeStamp();
            long newTimeStamp = newData.getRotationTimeStamp();
            // This timestep's delta rotation to be multiplied by the current rotation after computing it from the gyro sample data.
            if (oldTimeStamp != 0) {
                final float dT = (newTimeStamp - oldTimeStamp) * NS2S;
                // Axis of the rotation sample, not normalized yet.
                float axisX = newData.getRotationRawEvent()[0];
                float axisY = newData.getRotationRawEvent()[1];
                float axisZ = newData.getRotationRawEvent()[2];
                // Calculate the angular speed of the sample
                float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);
                // Normalize the rotation vector if it's big enough to get the axis
                // (that is, EPSILON should represent your maximum allowable margin of error)
                if (omegaMagnitude > EPSILON) {
                    axisX /= omegaMagnitude;
                    axisY /= omegaMagnitude;
                    axisZ /= omegaMagnitude;
                }

                // Integrate around this axis with the angular speed by the timestep
                // in order to get a delta rotation from this sample over the timestep
                // We will convert this axis-angle representation of the delta rotation
                // into a quaternion before turning it into the rotation matrix.
                float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
                float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
                deltaRotationVector[0] = sinThetaOverTwo * axisX;
                deltaRotationVector[1] = sinThetaOverTwo * axisY;
                deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                deltaRotationVector[3] = cosThetaOverTwo;
            }

            oldTimeStamp = newTimeStamp;
            float[] deltaRotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
            Log.d(CAPTURELOGTAG, "Current Rotation:" + Arrays.toString(deltaRotationMatrix));
            // User code should concatenate the delta rotation we computed with the current rotation
            // in order to get the updated rotation.
            // rotationCurrent = rotationCurrent * deltaRotationMatrix;
        }

    }


    public void logTimeStampDataOnFile(Context context, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Hyperlapse Timestamps");
            if (!root.exists()) {
                root.mkdirs();
            }
            String fileName = String.valueOf(System.currentTimeMillis()) + ".txt";
            File gpxfile = new File(root, fileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logSensorValues(String sensorType, SensorEvent event) {
        StringBuffer sb = new StringBuffer(sensorType + "values: ");
        for (int i = 0; i < event.values.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(event.values[i]);
        }
        Log.d(SENSORSTAG, "onSensorChanged " + sb.toString());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.equals(this.accelerometer)) {
            Integer accelerometerAccuracy = accuracy;
        } else if (sensor.equals(this.gyroscope)) {
            Integer gyroscopeAccuracy = accuracy;
        } else if (sensor.equals(this.rotation)) {
            Integer rotationAccuracy = accuracy;
        }
    }

    private float[] adjustAccelerometerValues(int displayRotation, @NonNull float[] eventValues) {
        float[] adjustedValues = new float[3];

        final int axisSwap[][] = {
                {1, -1, 0, 1},  // ROTATION_0
                {-1, -1, 1, 0}, // ROTATION_90
                {-1, 1, 0, 1},  // ROTATION_180
                {1, 1, 1, 0}    // ROTATION_270
        };

        final int[] as = axisSwap[displayRotation];
        adjustedValues[0] = (float) as[0] * eventValues[as[2]];
        adjustedValues[1] = (float) as[1] * eventValues[as[3]];
        adjustedValues[2] = eventValues[2];

        return adjustedValues;
    }

    protected int getDisplayRotation() {
        Log.d(SENSORSTAG, "getDisplayRotation");
        return getActivity().getWindowManager().getDefaultDisplay().getRotation();
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (DEFAULT_ORIENTATIONS.get(rotation) + this.sensorOrientation + 270) % 360;
    }

    /**
     * @param accelerometerValues raw {@code float[3]} accelerometer values
     * @return {@link android.view.Surface} rotation constant indicating the current screen orientation
     */
    private int getRotationFromAccelerometer(@NonNull float[] accelerometerValues) {
        float[] adjustedValues = this.adjustAccelerometerValues(getDisplayRotation(), accelerometerValues);
        if (adjustedValues[0] >= -4.9f && adjustedValues[0] <= 4.9f) {
            if (adjustedValues[1] <= 0.0f) {
                Log.d(SENSORSTAG, "Surface.ROTATION_0, portrait");
                return Surface.ROTATION_0; // portrait
            }
            Log.d(SENSORSTAG, "Surface.ROTATION_180, reverse-portrait");
            return Surface.ROTATION_180; // reverse-portrait
        }
        if (adjustedValues[0] <= 0.0f) {
            Log.d(SENSORSTAG, "Surface.ROTATION_270, reverse-landscape");
            return Surface.ROTATION_270; // reverse-landscape
        }
        Log.d(SENSORSTAG, "Surface.ROTATION_90, landscape");
        return Surface.ROTATION_90; // defaults to landscape
    }

    /**
     * Initialize sensors for data collection
     */
    private void initSensors() {
        this.sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        // The accuracy of this sensor is lower than the normal rotation vector sensor,
        // but the power consumption is reduced. Better for background processing
        //this.gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        this.rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mRotationMatrix[0] = 1;
        mRotationMatrix[5] = 1;
        mRotationMatrix[10] = 1;
        mRotationMatrix[15] = 1;
    }

    /**
     * Register sensors with sensor manager
     */
    private void registerSensors() {
        Log.d(SENSORSTAG, "registerSensors");
        if (this.accelerometer != null) {
            Log.d(SENSORSTAG, "registerSensors: Accelerometer");
            this.sensorManager
                    .registerListener(this, this.accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Log.d(SENSORSTAG, "registerSensors: Accelerometer not available!");
        }

        if (this.gyroscope != null) {
            Log.d(SENSORSTAG, "registerSensors: Gyroscope");
            this.sensorManager.registerListener(this, this.gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Log.d(SENSORSTAG, "registerSensors: Gyroscope not available!");
        }

        if (this.rotation != null) {
            Log.d(SENSORSTAG, "registerSensors: Rotation vector");
            this.sensorManager.registerListener(this, this.rotation, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Log.d(SENSORSTAG, "registerSensors: Rotation vector not available!");
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets whether you should show UI with rationale for requesting permissions.
     *
     * @param permissions The permissions your app wants to request.
     * @return Whether you can show permission rationale UI.
     */
    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS)) {

            } else {

            }
        }
        return false;
    }

    /**
     * Requests permissions needed for recording video.
     */
    private void requestVideoPermissions() {
        if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS)) {
            //new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.length == VIDEO_PERMISSIONS.length) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        ErrorDialog.newInstance(getString(R.string.permission_request))
                                .show(getChildFragmentManager(), FRAGMENT_DIALOG);
                        break;
                    }
                }
            } else {
                ErrorDialog.newInstance(getString(R.string.permission_request))
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     */
    private static class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        public ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}
