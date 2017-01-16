package com.sand5.videostabilize.hyperlapse.camera2.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Matrix;
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
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
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
import com.sand5.videostabilize.hyperlapse.camera2.utils.AutoFitTextureView;
import com.sand5.videostabilize.hyperlapse.camera2.utils.CameraMetaDataHelper;
import com.sand5.videostabilize.hyperlapse.camera2.utils.CameraSizeUtils;

import org.json.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.sand5.videostabilize.hyperlapse.camera2.utils.CameraParameterLogging.logAllCalibrationData;

public class CameraFragment extends Fragment
        implements View.OnClickListener, FragmentCompat.OnRequestPermissionsResultCallback,
        SensorEventListener {

    // TODO: 1/13/17 Start by checking hardware device level for camera parameters and sensors
    // TODO: 1/13/17 Add gravity, geo-magnet and linear acceleration sensors

    //tags
    private static final String TAG = "Camera2VideoFragment";
    private static final String SENSORSTAG = "SensorsParameters";
    private static final String CAPTURELOGTAG = "CaptureParameters";


    //orientation variablies
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    //permissions
    private static final int REQUEST_VIDEO_PERMISSIONS = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};

    private static final String SENSORTIMESTAMPLOG = "SensorTimeStampLog";
    private static final String FRAMETIMESTAPLOGTAG = "FrameTimeStampLog";

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
    long systemCurrentTimeMillis;
    long nanoTime;
    long elapsedRealtimeNanos;
    long accelerometerTimeStamp;
    long gyroscopeTimeStamp;
    long rotationTimeStamp;
    ArrayList<Long> frameTimeStampDelta = new ArrayList<>();
    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;
    private Button mButtonVideo;
    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;
    /**
     * A reference to the current {@link CameraCaptureSession} for
     * preview.
     */
    private CameraCaptureSession mPreviewSession;
    /**
     * The {@link Size} of camera preview.
     */
    private Size mPreviewSize;
    /**
     * The {@link Size} of video recording.
     */
    private Size mVideoSize;
    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;
    /**
     * Whether the app is recording video now
     */
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
    private String mNextVideoAbsolutePath;
    private CaptureRequest.Builder mPreviewBuilder;
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
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor rotation;
    private JSONArray gyroscopeTimeStamps;
    private JSONArray frameTimeStamps;

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera2_video, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        mButtonVideo = (Button) view.findViewById(R.id.video);
        gyroscopeTimeStamps = new JSONArray();
        frameTimeStamps = new JSONArray();
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
     * Get Hardware level of camera so we know which features are supported
     *
     * @param characteristics
     */
    private void getHardwareLevel(CameraCharacteristics characteristics) {
        CameraMetaDataHelper.getHardwareLevelName(characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL));

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

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            logAllCalibrationData(getActivity(), characteristics, mTextureView);

            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            sensorOrientation = mSensorOrientation;

            assert map != null;
            mVideoSize = CameraSizeUtils.chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
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

            Surface previewSurface = new Surface(texture);
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
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
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
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Log.d(CAPTURELOGTAG, "onCaptureCompleted");
                    Log.d(CAPTURELOGTAG, "Rolling shutter skew: " + result.get(CaptureResult.SENSOR_ROLLING_SHUTTER_SKEW));

                    long nano = System.nanoTime();
                    long elapsedNanos = SystemClock.elapsedRealtimeNanos();
                    long systemCurrentMillis = System.currentTimeMillis();
                    long frameTimeStamp = result.get(CaptureResult.SENSOR_TIMESTAMP);

                    frameTimeStampDelta.add(0, frameTimeStamp);
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
                    /*List<CaptureResult> partialResult = result.getPartialResults();
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
                    //printTimeStampData();
                    calculateTimeStep();

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

    private void calculateTimeStep() {
        for (int i = 0; i < frameTimeStampDelta.size(); i++) {
            long nextStamp = frameTimeStampDelta.get(i + 1);
            long previousStamp = frameTimeStampDelta.get(i);
            long timeStamp = nextStamp - previousStamp;
            Log.d(CAPTURELOGTAG, "TIME STEP:" + timeStamp);
        }
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
        File root = new File(Environment.getExternalStorageDirectory(), "Hyperlapse Timestamps");
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
        }

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
            setUpMediaRecorder();
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
            Surface mRecorderSurface = mMediaRecorder.getSurface();
            surfaces.add(mRecorderSurface);
            mPreviewBuilder.addTarget(mRecorderSurface);

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
                            mMediaRecorder.start();
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
        } catch (IOException e) {
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
        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
            mNextVideoAbsolutePath = getVideoFilePath(getActivity());
        }
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
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

    private String getVideoFilePath(Context context) {
        return context.getExternalFilesDir(null).getAbsolutePath() + "/"
                + System.currentTimeMillis() + ".mp4";
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
        mMediaRecorder.stop();
        mMediaRecorder.reset();

        Activity activity = getActivity();
        if (null != activity) {
            Toast.makeText(activity, "Video saved: " + mNextVideoAbsolutePath,
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Video saved: " + mNextVideoAbsolutePath);
        }
        mNextVideoAbsolutePath = null;
        startPreview();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (this.accelerometer != null && event.sensor.equals(this.accelerometer)) {
            float[] accelerometerValues = event.values;
            accelerometerTimeStamp = event.timestamp;
            Log.d(SENSORSTAG, "Accelerometer Timestamp:" + event.timestamp);
            logSensorValues("Accelerometer", event);
        } else if (this.gyroscope != null && event.sensor.equals(this.gyroscope)) {

            float[] gyroscopeValues = event.values;
            //Gyroscope timestamp
            gyroscopeTimeStamp = event.timestamp;
            //Current time
            systemCurrentTimeMillis = System.currentTimeMillis();
            //Most precise timestamp, with 0 value being when device was last rebooted, used to measure delta with another timestamp on same device.
            nanoTime = System.nanoTime();
            //Nanoseconds since boot
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos();

            String timeStampData = "SystemCurrentTimeMillis: " + systemCurrentTimeMillis + "\n" +
                    "SystemNano: " + nanoTime + "\n" +
                    "SystemElapsedRTNanos: " + elapsedRealtimeNanos + "\n" +
                    "Gyroscope: " + gyroscopeTimeStamp + "\n" +
                    "SystemNano - Gyroscope delta: " + (nanoTime - gyroscopeTimeStamp) + "\n" +
                    "SystemElapsed - Gyroscope delta: " + (elapsedRealtimeNanos - gyroscopeTimeStamp) + "\n";

            //logTimeStampDataOnFile(getActivity(),timeStampData);

            if (mIsRecordingVideo)
                gyroscopeTimeStamps.put(timeStampData);
            //Log.d(SENSORTIMESTAMPLOG, timeStampData);


            Log.d(SENSORSTAG, "Gyroscope Timestamp:" + event.timestamp);
            logSensorValues("Gyroscope", event);
        } else if (this.rotation != null && event.sensor.equals(this.rotation)) {
            float[] rotationValues = event.values;
            rotationTimeStamp = event.timestamp;
            Log.d(SENSORSTAG, "Rotation Timestamp:" + event.timestamp);
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, event.values);
            Log.d(SENSORSTAG, "Rotation Matrix: " + Arrays.toString(mRotationMatrix));
            logSensorValues("Rotation", event);
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

    private void initSensors() {
        Log.d(SENSORSTAG, "initSensors");
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
            if (FragmentCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Requests permissions needed for recording video.
     */
    private void requestVideoPermissions() {
        if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            FragmentCompat.requestPermissions(this, VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
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


    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.permission_request)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FragmentCompat.requestPermissions(parent, VIDEO_PERMISSIONS,
                                    REQUEST_VIDEO_PERMISSIONS);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    parent.getActivity().finish();
                                }
                            })
                    .create();
        }

    }

}
