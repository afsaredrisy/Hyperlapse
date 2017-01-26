package com.sand5.videostabilize.hyperlapse.camera2.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.VideoPicker;
import com.kbeanie.multipicker.api.callbacks.VideoPickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenVideo;
import com.orhanobut.logger.Logger;
import com.sand5.videostabilize.hyperlapse.R;
import com.sand5.videostabilize.hyperlapse.camera2.beans.AccelerometerData;
import com.sand5.videostabilize.hyperlapse.camera2.beans.GyroscopeData;
import com.sand5.videostabilize.hyperlapse.camera2.beans.IntrinsicMatrix;
import com.sand5.videostabilize.hyperlapse.camera2.beans.RotationVectorData;
import com.sand5.videostabilize.hyperlapse.camera2.utils.SampleMediaCodec;

import org.greenrobot.eventbus.EventBus;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoDecoderActivity extends AppCompatActivity {

    private static final String TAG = "VideoDecoderActivity";
    @BindView(R.id.yuv_mat_test_imageView)
    ImageView yuvMatTestImageView;
    SampleMediaCodec sampleMediaCodec;
    private ArrayList<ArrayList> bundleList = new ArrayList<>();
    private ArrayList<GyroscopeData> gyroscopeDataArrayList = new ArrayList<>();
    private ArrayList<RotationVectorData> rotationVectorDataArrayList = new ArrayList<>();
    private ArrayList<AccelerometerData> accelerometerDataArrayList = new ArrayList<>();
    private ArrayList<Mat> imageArrayList = new ArrayList<>();
    private IntrinsicMatrix intrinsicMatrix;
    private SurfaceView videoSurfaceView;
    private SurfaceHolder surfaceHolder;
    private Surface surface;
    private VideoPicker videoPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_decoder);
        ButterKnife.bind(this);
        getDataFromCamera();
    }

    private void getDataFromCamera() {
        bundleList = EventBus.getDefault().removeStickyEvent(ArrayList.class);
        if (null != bundleList) {
            Logger.d(bundleList.size());
            Logger.d("Bundle is not null");
            gyroscopeDataArrayList = bundleList.get(0);
            accelerometerDataArrayList = bundleList.get(1);
            rotationVectorDataArrayList = bundleList.get(2);
            imageArrayList = bundleList.get(3);
            getMatFromYuv(imageArrayList.get(0));
            Logger.d(imageArrayList.size());
        } else {
            Logger.d("Bundle is null");
        }
        intrinsicMatrix = EventBus.getDefault().removeStickyEvent(IntrinsicMatrix.class);
        if (null != intrinsicMatrix) {
            Logger.d("Matrix is not null");
        } else {
            Logger.d("Matrix is null");
        }

        //pickVideo();
    }

    private void getMatFromYuv(Mat bgrMat) {

        Mat rgbaMatOut = new Mat();
        Imgproc.cvtColor(bgrMat, rgbaMatOut, Imgproc.COLOR_BGR2RGBA, 0);
        Bitmap bitmap = Bitmap.createBitmap(bgrMat.cols(), bgrMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgbaMatOut, bitmap);
        yuvMatTestImageView.setImageBitmap(bitmap);

        /* //Mat mRgbaMat = new Mat();
        Mat mRgbaMat = new Mat();
        Imgproc.cvtColor( mYUVMat, mRgbaMat, Imgproc.COLOR_YUV420sp2RGBA,4);
        // Draw Bitmap New:
        Bitmap mBitmap = Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap( mRgbaMat, mBitmap);
        yuvMatTestImageView.setImageBitmap(mBitmap);
        mRgbaMat.release();*/


        /*
        // Spec guarantees that planes[0] is luma and has pixel stride of 1.
        // It also guarantees that planes[1] and planes[2] have the same row and
        // pixel stride.
        if (planes[1].getPixelStride() != 1 && planes[1].getPixelStride() != 2) {
            throw new IllegalArgumentException(
                    "src chroma plane must have a pixel stride of 1 or 2: got "
                            + planes[1].getPixelStride());

        }*/
    }

    private void decodeVideo(String videoPath) {
        sampleMediaCodec = new SampleMediaCodec(this, surface, videoPath);
        videoSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = videoSurfaceView.getHolder();
        surface = surfaceHolder.getSurface();
        sampleMediaCodec.play();
    }

    private void pickVideo() {
        Log.d(TAG, "Pick Video");
        videoPicker = new VideoPicker(this);
        videoPicker.setVideoPickerCallback(new VideoPickerCallback() {

                                               @Override
                                               public void onError(String s) {
                                                   Logger.d("OnVideoError");
                                                   Log.d(TAG, "Error: " + s.getBytes().toString());
                                               }

                                               @Override
                                               public void onVideosChosen(List<ChosenVideo> list) {
                                                   Logger.d("OnVideosChosen");
                                                   ChosenVideo video = list.get(0);
                                                   String videoUri = video.getQueryUri();
                                                   String videoPath = video.getOriginalPath();
                                                   Log.d(TAG, "VideoURI: " + videoUri);
                                                   Log.d(TAG, "VideoPath: " + videoPath);
                                                   decodeVideo(videoPath);
                                               }
                                           }
        );
// videoPicker.allowMultiple(); // Default is false
// videoPicker.shouldGenerateMetadata(false); // Default is true
        videoPicker.shouldGeneratePreviewImages(false); // Default is true
        videoPicker.pickVideo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Picker.PICK_VIDEO_DEVICE) {
                videoPicker.submit(data);
            }
        }
    }
}
