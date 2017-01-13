package com.sand5.videostabilize.hyperlapse;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sand5.videostabilize.hyperlapse.camera2.activities.CameraActivity;
import com.sand5.videostabilize.hyperlapse.tests.SensorFusionActivity;
import com.sand5.videostabilize.hyperlapse.tests.cameracalibration.CameraCalibrationActivity;
import com.sand5.videostabilize.hyperlapse.tests.postprocessing.PostProcessingActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    /*static {
        System.loadLibrary("native-lib");
    }*/

    private static final String TAG = "MainActivity";

    @BindView(R.id.post_processing)
    Button postProcessing;

    @BindView(R.id.calibration_camera_activity)
    Button calibrate;

    @BindView(R.id.opticalFlow_camera_activity)
    Button opticalFlow;

    @BindView(R.id.sensorFusion_camera_activity)
    Button sensorFusion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        PermissionGen.with(MainActivity.this)
                .addRequestCode(100)
                .permissions(
                        Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE)
                .request();
        // Example of a call to a native method
        /*TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());*/
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @OnClick({R.id.post_processing, R.id.camera2_camera_activity, R.id.calibration_camera_activity, R.id.opticalFlow_camera_activity, R.id.sensorFusion_camera_activity})
    public void openActivity(View v) {
        switch (v.getId()) {
            case R.id.post_processing:
                startActivity(new Intent(this, PostProcessingActivity.class));
                break;
            case R.id.calibration_camera_activity:
                startActivity(new Intent(this, CameraCalibrationActivity.class));
                break;
            case R.id.opticalFlow_camera_activity:
                startActivity(new Intent(this, PostProcessingActivity.class));
                break;
            case R.id.sensorFusion_camera_activity:
                startActivity(new Intent(this, SensorFusionActivity.class));
                break;
            case R.id.camera2_camera_activity:
                startActivity(new Intent(this, CameraActivity.class));
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @PermissionSuccess(requestCode = 100)
    public void doSomething() {
        Log.d(TAG, "Permission granted,yay!");
    }

    @PermissionFail(requestCode = 100)
    public void doFailSomething() {
        Log.d(TAG, "Permission denied,daym!");
    }


}
