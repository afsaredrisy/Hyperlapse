package com.sand5.videostabilize.hyperlapse;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sand5.videostabilize.hyperlapse.camera2.activities.CameraActivity;
import com.sand5.videostabilize.hyperlapse.sensors.SensorTestActivity;
import com.sand5.videostabilize.hyperlapse.tests.cameracalibration.OpticalFlowTestActivity;
import com.sand5.videostabilize.hyperlapse.tests.postprocessing.PostProcessingActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    /*static {
        System.loadLibrary("native-lib");
    }*/

    private static final String TAG = "MainActivity";
    private static final int PERM_WRITE_STORAGE = 101;

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
                startActivity(new Intent(this, OpticalFlowTestActivity.class));
                break;
            case R.id.opticalFlow_camera_activity:
                startActivity(new Intent(this, PostProcessingActivity.class));
                break;
            case R.id.sensorFusion_camera_activity:
                startActivity(new Intent(this, SensorTestActivity.class));
                break;
            case R.id.camera2_camera_activity:
                startActivity(new Intent(this, CameraActivity.class));
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(PERM_WRITE_STORAGE)
    private void smsTask() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Have permission, do the thing!
            Toast.makeText(this, "Write permission granted", Toast.LENGTH_LONG).show();
        } else {
            // Request one permission
            EasyPermissions.requestPermissions(this, getString(R.string.permission_camera_rationale),
                    PERM_WRITE_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }


}
