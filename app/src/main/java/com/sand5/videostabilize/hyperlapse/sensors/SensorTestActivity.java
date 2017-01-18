package com.sand5.videostabilize.hyperlapse.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sand5.videostabilize.hyperlapse.R;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SensorTestActivity extends AppCompatActivity implements SensorEventListener2 {

    private static final String SENSORSTAG = "SensorTag";
    private static final String TAG = "SensorTestActivity";
    private final float[] mRotationMatrix = new float[16];
    @BindView(R.id.start_plot)
    Button startPlot;
    @BindView(R.id.stop_plot)
    Button stopPlot;
    @BindView(R.id.activity_sensor_test)
    LinearLayout activitySensorTest;
    private PrintStream mGyroFile;
    private ArrayList<TestAccelerometerData> testAccelerometerDataArrayList;
    private ArrayList<TestGyroscopeData> testGyroscopeDataArrayList;
    private ArrayList<RotationData> rotationDataArrayList;
    private SensorManager mSensorManager;
    private Sensor accelerometer, gyroscope, rotation;
    private float[] accelerometerValues, gyroscopeValues, rotationValues, gravityValues, geoMagneticValues;
    private boolean shouldPlot = false;
    private long accelerometerTimeStamp, gyroscopeTimeStamp, rotationTimeStamp, systemCurrentTimeMillis, nanoTime, elapsedRealtimeNanos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_test);
        ButterKnife.bind(this);
        testAccelerometerDataArrayList = new ArrayList<>();
        testGyroscopeDataArrayList = new ArrayList<>();
        rotationDataArrayList = new ArrayList<>();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        initSensors();
        initCSVFiles();
        startPlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldPlot = true;
            }
        });

        stopPlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldPlot = false;
                printGyroscopeDataToCSV();
            }
        });
    }

    private void initSensors() {
        Log.d(SENSORSTAG, "initSensors");
        this.mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        this.rotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mRotationMatrix[0] = 1;
        mRotationMatrix[5] = 1;
        mRotationMatrix[10] = 1;
        mRotationMatrix[15] = 1;
    }


    private void registerSensors() {
        Log.d(SENSORSTAG, "registerSensors");
        if (this.accelerometer != null) {
            Log.d(SENSORSTAG, "registerSensors: Accelerometer");
            this.mSensorManager
                    .registerListener(this, this.accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Log.d(SENSORSTAG, "registerSensors: Accelerometer not available!");
        }

        if (this.gyroscope != null) {
            Log.d(SENSORSTAG, "registerSensors: Gyroscope");
            this.mSensorManager.registerListener(this, this.gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Log.d(SENSORSTAG, "registerSensors: Gyroscope not available!");
        }

        if (this.rotation != null) {
            Log.d(SENSORSTAG, "registerSensors: Rotation vector");
            this.mSensorManager.registerListener(this, this.rotation, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Log.d(SENSORSTAG, "registerSensors: Rotation vector not available!");
        }
    }

    @Override
    public void onFlushCompleted(Sensor sensor) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (this.accelerometer != null && event.sensor.equals(this.accelerometer)) {
            this.accelerometerValues = event.values;
            accelerometerTimeStamp = event.timestamp;
            long systemCurrentTimeMillis = System.currentTimeMillis();
            long nanoTime = System.nanoTime();
            long elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos();
            /*if (shouldPlot) {
                AccelerometerData data = new AccelerometerData(systemCurrentTimeMillis, nanoTime, elapsedRealtimeNanos, accelerometerTimeStamp, accelerometerValues);
                accelerometerDataArrayList.add(0, data);
            }*/


            //Log.d(SENSORSTAG, "Accelerometer Timestamp:" + event.timestamp);
            //logSensorValues("Accelerometer", event);
        } else if (this.gyroscope != null && event.sensor.equals(this.gyroscope)) {
            this.gyroscopeValues = event.values;
            gyroscopeTimeStamp = event.timestamp;
            long systemCurrentTimeMillis = System.currentTimeMillis();
            long nanoTime = System.nanoTime();
            long elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos();
            if (shouldPlot) {
                // GyroscopeData data = new GyroscopeData(systemCurrentTimeMillis, nanoTime, elapsedRealtimeNanos, gyroscopeTimeStamp, gyroscopeValues);
                //gyroscopeDataArrayList.add(0, data);
                mGyroFile.append(systemCurrentTimeMillis + "," + nanoTime + "," + elapsedRealtimeNanos + "," + (nanoTime - event.timestamp) + "," + (elapsedRealtimeNanos - event.timestamp) + "," + gyroscopeValues[0] + "," +
                        gyroscopeValues[1] + "," +
                        gyroscopeValues[2] + "," +
                        (event.timestamp) + "\n");
            }
            /*String timeStampData = "SystemCurrentTimeMillis: " + systemCurrentTimeMillis + "\n" +
                    "SystemNano: " + nanoTime + "\n" +
                    "SystemElapsedRTNanos: " + elapsedRealtimeNanos + "\n" +
                    "Gyroscope: " + gyroscopeTimeStamp + "\n" +
                    "SystemNano - Gyroscope delta: " + (nanoTime - gyroscopeTimeStamp) + "\n" +
                    "SystemElapsed - Gyroscope delta: " + (elapsedRealtimeNanos - gyroscopeTimeStamp) + "\n";
            logTimeStampDataOnFile(getActivity(),timeStampData);
            if (shouldPlot)
                gyroscopeTimeStamps.put(timeStampData);
            Log.d(SENSORTIMESTAMPLOG, timeStampData);
            Log.d(SENSORSTAG, "Gyroscope Timestamp:" + event.timestamp);
            logSensorValues("Gyroscope", event);*/
        } else if (this.rotation != null && event.sensor.equals(this.rotation)) {
            this.rotationValues = event.values;
            rotationTimeStamp = event.timestamp;
            long systemCurrentTimeMillis = System.currentTimeMillis();
            long nanoTime = System.nanoTime();
            long elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos();
           /* if (shouldPlot) {
                RotationData data = new RotationData(systemCurrentTimeMillis, nanoTime, elapsedRealtimeNanos, rotationTimeStamp, rotationValues);
                rotationDataArrayList.add(0, data);
            }*/

            //Log.d(SENSORSTAG, "Rotation Timestamp:" + event.timestamp);
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, event.values);
            //Log.d(SENSORSTAG, "Rotation Matrix: " + Arrays.toString(mRotationMatrix));
            //logSensorValues("Rotation", event);
        }
    }

    private void initCSVFiles() {
        try {
            mGyroFile = new PrintStream(getOutputGyroFile());
            mGyroFile.append("systemCurrentTimeMillis,nanoTime,elapsedRealTimeNanos,nanoDelta,elapsedDelta,gyroX,gyroY,gyroZ\n");
        } catch (IOException e) {
            Log.d(TAG, "Unable to create acquisition file");
        }
    }

    private void printGyroscopeDataToCSV() {
        mGyroFile.close();
        Toast.makeText(this, "Gyro Saved", Toast.LENGTH_SHORT).show();
    }


    private void plotAccelerometerValues() {

    }

    private void plotGyroscopeValues() {

    }

    private void plotRotationValues() {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    private File getOutputGyroFile() {
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        File gyroStorageDir = new File(Environment.getExternalStorageDirectory(), "Hyperlapse data");

        if (!gyroStorageDir.exists()) {
            if (!gyroStorageDir.mkdirs()) {
                Log.d("Recorder", "Failed to create directory");
                return null;
            }
        }
        File gyroFile;
        gyroFile = new File(gyroStorageDir.getPath() + File.separator + "gyro.csv");
        return gyroFile;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSensors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }


    /*
    Code to integrate gyroscope values over a given timestep
    // Create a constant to convert nanoseconds to seconds.
private static final float NS2S = 1.0f / 1000000000.0f;
private final float[] deltaRotationVector = new float[4]();
private float timestamp;

public void onSensorChanged(SensorEvent event) {
  // This timestep's delta rotation to be multiplied by the current rotation
  // after computing it from the gyro sample data.
  if (timestamp != 0) {
    final float dT = (event.timestamp - timestamp) * NS2S;
    // Axis of the rotation sample, not normalized yet.
    float axisX = event.values[0];
    float axisY = event.values[1];
    float axisZ = event.values[2];

    // Calculate the angular speed of the sample
    float omegaMagnitude = sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

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
    float sinThetaOverTwo = sin(thetaOverTwo);
    float cosThetaOverTwo = cos(thetaOverTwo);
    deltaRotationVector[0] = sinThetaOverTwo * axisX;
    deltaRotationVector[1] = sinThetaOverTwo * axisY;
    deltaRotationVector[2] = sinThetaOverTwo * axisZ;
    deltaRotationVector[3] = cosThetaOverTwo;
  }
  timestamp = event.timestamp;
  float[] deltaRotationMatrix = new float[9];
  SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
    // User code should concatenate the delta rotation we computed with the current rotation
    // in order to get the updated rotation.
    // rotationCurrent = rotationCurrent * deltaRotationMatrix;
   }
}
     */
}
