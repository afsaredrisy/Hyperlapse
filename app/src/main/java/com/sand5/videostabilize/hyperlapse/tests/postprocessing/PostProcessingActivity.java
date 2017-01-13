package com.sand5.videostabilize.hyperlapse.tests.postprocessing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.VideoPicker;
import com.kbeanie.multipicker.api.callbacks.VideoPickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenVideo;
import com.sand5.videostabilize.hyperlapse.R;

import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.common.FileChannelWrapper;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.TrackType;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.Transform;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import hp.harsh.library.interfaces.PermissionInterface;
import hp.harsh.library.manager.PermissionRequest;
import hp.harsh.library.manager.PermissionResponse;
import hp.harsh.library.utilbag.Permission;
import hp.harsh.library.utilbag.PermissionCode;

import static org.jcodec.scale.BitmapUtil.fromBitmap;

public class PostProcessingActivity extends AppCompatActivity implements PermissionInterface {


    private static final String TAG = "MainActivity";

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    private VideoPicker videoPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_processing);
        new PermissionRequest(PostProcessingActivity.this,
                Permission.PERMISSION_READ_EXTERNAL_STORAGE,
                PermissionCode.CODE_PERMISSION_READ_EXTERNAL_STORAGE,
                R.string.permission_camera_rationale,
                R.string.permission_camera_denied,
                R.string.permission_enable_message, PostProcessingActivity.this)
                .checkPermission();

        new PermissionRequest(PostProcessingActivity.this,
                Permission.PERMISSION_WRITE_EXTERNAL_STORAGE,
                PermissionCode.CODE_PERMISSION_WRITE_EXTERNAL_STORAGE,
                R.string.permission_camera_rationale,
                R.string.permission_camera_denied,
                R.string.permission_enable_message, PostProcessingActivity.this)
                .checkPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionCode.CODE_PERMISSION_READ_EXTERNAL_STORAGE:
                // Check granted permission for camera
                new PermissionRequest(PostProcessingActivity.this,
                        Permission.PERMISSION_READ_EXTERNAL_STORAGE,
                        PermissionCode.CODE_PERMISSION_READ_EXTERNAL_STORAGE,
                        R.string.permission_camera_rationale,
                        R.string.permission_camera_denied,
                        R.string.permission_enable_message, this)
                        .onRequestPermissionsResult(PostProcessingActivity.this, requestCode, grantResults);
                break;
            case PermissionCode.CODE_PERMISSION_WRITE_EXTERNAL_STORAGE:
                // Check granted permission for camera
                new PermissionRequest(PostProcessingActivity.this,
                        Permission.PERMISSION_WRITE_EXTERNAL_STORAGE,
                        PermissionCode.CODE_PERMISSION_WRITE_EXTERNAL_STORAGE,
                        R.string.permission_camera_rationale,
                        R.string.permission_camera_denied,
                        R.string.permission_enable_message, this)
                        .onRequestPermissionsResult(PostProcessingActivity.this, requestCode, grantResults);
                break;
        }
    }

    @Override
    public void onGranted(PermissionResponse permissionResponse) {
        switch (permissionResponse.type) {
            case PermissionCode.CODE_PERMISSION_READ_EXTERNAL_STORAGE:
                pickVideo();
                break;

        }
    }

    private void processVideo(String absolutePath) {
        Log.d(TAG, "Processing video into frames");
        new ProcessFramesTask().execute(absolutePath);
        //loadFFMPEGbinary();
    }

    private void pickVideo() {
        Log.d(TAG, "Pick Video");
        videoPicker = new VideoPicker(PostProcessingActivity.this);
        videoPicker.setVideoPickerCallback(new VideoPickerCallback() {

                                               @Override
                                               public void onError(String s) {
                                                   Log.d(TAG, "Error: " + s.getBytes().toString());
                                               }

                                               @Override
                                               public void onVideosChosen(List<ChosenVideo> list) {
                                                   ChosenVideo video = list.get(0);
                                                   String videoUri = video.getQueryUri();
                                                   String videoPath = video.getOriginalPath();
                                                   processVideo(videoPath);
                                                   Log.d(TAG, "VideoURI: " + videoUri);
                                                   Log.d(TAG, "VideoPath: " + videoPath);
                                               }
                                           }
        );
// videoPicker.allowMultiple(); // Default is false
// videoPicker.shouldGenerateMetadata(false); // Default is true
        videoPicker.shouldGeneratePreviewImages(false); // Default is true
        videoPicker.pickVideo();
    }

    private void loadFFMPEGbinary() {
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Log.d(TAG, "FFMPEG: On Start");
                }

                @Override
                public void onFailure() {
                    Log.d(TAG, "FFMPEG: On Failure");
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "FFMPEG: On Success");
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "FFMPEG: On Finish");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            Log.d(TAG, "FFMPEG not supported: " + e.getLocalizedMessage());
            // Handle if FFmpeg is not supported by device
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Picker.PICK_VIDEO_DEVICE) {
                videoPicker.submit(data);
            }
        }
    }

    private class ProcessFramesTask extends AsyncTask<String, Integer, ArrayList<Bitmap>> {
        @Override
        protected ArrayList<Bitmap> doInBackground(String... strings) {
            ArrayList<Bitmap> frameList;
      /* MediaMetadataRetriever class is used to retrieve meta data from methods. */
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                //path of the video of which you want frames
                retriever.setDataSource(strings[0]);
            } catch (Exception e) {
                System.out.println("Exception= " + e);
            }
            // created an arraylist of bitmap that will store your frames
            frameList = new ArrayList<Bitmap>();
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            int duration_millisec = Integer.parseInt(duration); //duration in millisec
            int duration_second = duration_millisec / 1000;  //millisec to sec.
            int frames_per_second = 2;  //no. of frames want to retrieve per second
            int numberOfFrameCaptured = frames_per_second * duration_second;
            for (int i = 0; i < 50; i++) {
                Log.d(TAG, "Processing frame number: " + i);
                Bitmap inputBitmap = retriever.getFrameAtTime(5000 * i);
                Mat sampleMat = new Mat(inputBitmap.getHeight(), inputBitmap.getWidth(), CvType.CV_8UC4);
                Utils.bitmapToMat(retriever.getFrameAtTime(5000 * i), sampleMat);
                //setting time position at which you want to retrieve frames
                frameList.add(retriever.getFrameAtTime(5000 * i));
            }
            Log.d(TAG, "Number of frames: " + frameList.size());
            return frameList;
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> bitmaps) {
            super.onPostExecute(bitmaps);
            Log.d(TAG, "Done video processing!");
            new StitchVideoTask().execute(bitmaps);
        }
    }


    private class StitchVideoTask extends AsyncTask<ArrayList<Bitmap>, Integer, Void> {

        @Override
        protected Void doInBackground(ArrayList<Bitmap>... arrayLists) {
            ArrayList<Bitmap> frameList = arrayLists[0];
            Log.d(TAG, "Stitch Video started");
            Transform transform = null;
            int timescale = 13000;
            int speed = 1000;
            FileChannelWrapper ch = null;
            MP4Muxer muxer = null;
            H264Encoder encoder = new H264Encoder();
            ByteBuffer _out = ByteBuffer.allocate(640 * 480 * 6);
            ArrayList<ByteBuffer> spsList = new ArrayList<ByteBuffer>();
            ArrayList<ByteBuffer> ppsList = new ArrayList<ByteBuffer>();
            File f = null;

            int progress = 0;
            try {
                String extr = Environment.getExternalStorageDirectory().toString();
                File mFolder = new File(extr + "/Hyperlapse");
                if (!mFolder.exists()) {
                    mFolder.mkdir();
                }

                String fileName = System.currentTimeMillis() + ".mp4";
                f = new File(mFolder, fileName);
                ch = NIOUtils.writableFileChannel(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                muxer = new MP4Muxer(ch, Brand.MP4);
            } catch (IOException e) {
                e.printStackTrace();
            }
            FramesMP4MuxerTrack outTrack = null;
            if (muxer != null) {
                outTrack = muxer.addTrack(TrackType.VIDEO, timescale);
            }
            //Scale image down to size chosen by user.
            int i = 0;
            for (Bitmap testBitmap : frameList) {
                Log.d(TAG, "Inside stitching for-loop");
                Picture img = fromBitmap(testBitmap);
                Picture yuv = Picture.create(img.getWidth(), img.getHeight(), ColorSpace.YUV420);
                transform = ColorUtil.getTransform(ColorSpace.RGB, encoder.getSupportedColorSpaces()[0]);
                transform.transform(img, yuv);

                img = null;
                _out.clear();
                ByteBuffer result = encoder.encodeFrame(yuv, _out); //toEncode

                // Based on the frame above form correct MP4 packet
                spsList.clear();
                ppsList.clear();
                H264Utils.wipePS(result, spsList, ppsList);
                H264Utils.encodeMOVPacket(result);
                try {
                    if (outTrack != null) {
                        outTrack.addFrame(new MP4Packet(result, i * speed, timescale, speed, i, true, null, i, 0));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                i++;
                result = null;
            }
            if (outTrack != null) {
                outTrack.addSampleEntry(H264Utils.createMOVSampleEntry(spsList, ppsList, 4));
            }
            try {
                if (muxer != null) {
                    muxer.writeHeader();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            NIOUtils.closeQuietly(ch);
            Log.d(TAG, "Stitching done!");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent mediaScanIntent = new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f); //out is your file you saved/deleted/moved/copied
                mediaScanIntent.setData(contentUri);
                PostProcessingActivity.this.sendBroadcast(mediaScanIntent);
            } else {
                PostProcessingActivity.this.sendBroadcast(new Intent(
                        Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://"
                                + Environment.getExternalStorageDirectory())));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "Finally Done!");
        }
    }
}
