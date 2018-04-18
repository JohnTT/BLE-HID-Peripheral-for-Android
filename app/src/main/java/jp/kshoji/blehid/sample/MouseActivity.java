package jp.kshoji.blehid.sample;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;

import jp.kshoji.blehid.MousePeripheral;
import jp.kshoji.blehid.sample.R.id;
import jp.kshoji.blehid.sample.R.layout;
import jp.kshoji.blehid.sample.R.string;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;

/**
 * Activity for BLE Mouse peripheral
 * 
 * @author K.Shoji
 */
public class MouseActivity extends AbstractBleActivity implements SensorEventListener {
    private final static String TAG = "MouseActivity";

    // IMU Stuff
    private SensorManager mSensorManager;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];
    private final float[] mPrevOrientationAngles = new float[3];

    public final double RAD_TO_DEGREE = (180.0/Math.PI);
    private final int RESOLUTION_X = 1080;
    private final int RESOLUTION_Y = 1920;
    private final int ANGLE_RANGE = 60;
    private final double IGNORE_STEP_BELOW = 20.0;


    boolean isSignChange(double x1, double x2) {
        if ((x1 * x2) < 0)
            return true;
        return false;
    }

    boolean isPiCrossing(double x1, double x2) {
        if (Math.abs(x1 - x2) > 180)
            return true;
        return false;
    }

    double getStep(double ref, double now) {
        double step = 0.0;
        // If there is a sign change
        if (isSignChange(ref, now) == true) {
            // Check if PI Crossing
            if (isPiCrossing(ref, now) == true) {
                if (ref > 0)
                    step = (ref - 180) + (-180 - now);
                else
                    step = (ref - (-180)) + (180 - now);
            }
            // Else Zero Crossing
            else {
                step = ref - now;
            }
        }
        // Else - Read how much we have moved with respect to reference in X
        else {
            step = ref - now;
        }
        return step;
    }

    // Moves cursor based off of orientation angles
    private void eulerMove() {
        // Log Sensor Data
        Log.v(TAG, "Now Euler X: " + mOrientationAngles[0]);
        Log.v(TAG, "Now Euler Y: " + mOrientationAngles[1]);
        Log.v(TAG, "Now Euler Z: " + mOrientationAngles[2]);

        // Log Sensor Data
        Log.v(TAG, "Prev Euler X: " + mPrevOrientationAngles[0]);
        Log.v(TAG, "Prev Euler Y: " + mPrevOrientationAngles[1]);
        Log.v(TAG, "Prev Euler Z: " + mPrevOrientationAngles[2]);

        double pointX = (getStep(mPrevOrientationAngles[0], mOrientationAngles[0])) * (RESOLUTION_X / ANGLE_RANGE);
        double pointY = (getStep(mPrevOrientationAngles[1], mOrientationAngles[1])) * (RESOLUTION_Y / ANGLE_RANGE);
        Log.v(TAG, "Point X: " + pointX);
        Log.v(TAG, "Point Y: " + pointY);


        mouse.movePointer((int) pointX, (int) -pointY, 0, false, false, false);
        mPrevOrientationAngles[0] = mOrientationAngles[0];
        mPrevOrientationAngles[1] = mOrientationAngles[1];
        mPrevOrientationAngles[2] = mOrientationAngles[2];




    }
    // Handlers
    long startTime = 0;
    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            EditText edtX = (EditText)findViewById(R.id.edtX);
            EditText edtY = (EditText)findViewById(R.id.edtY);
            EditText edtZ = (EditText)findViewById(R.id.edtZ);

            edtX.setText(mOrientationAngles[0]+"");
            edtY.setText(mOrientationAngles[1]+"");
            edtZ.setText(mOrientationAngles[2]+"");


            timerHandler.postDelayed(this, 10);
        }
    };



    private MousePeripheral mouse;
    private float X, Y, firstX, firstY;
    private int maxPointerCount;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_mouse);

        setTitle(getString(string.ble_mouse));


        mSensorManager = (SensorManager) MainActivity.getContext().getSystemService(SENSOR_SERVICE);


        final Button btnStartIMU = (Button)findViewById(R.id.btnStartIMU);
        btnStartIMU.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (btnStartIMU.getText().equals("Start IMU")) {
                    mSensorManager.registerListener(MouseActivity.this,
                            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
                    mSensorManager.registerListener(MouseActivity.this,
                            mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
                    startTime = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnable, 0);


                    btnStartIMU.setText("Stop IMU");
                }
                else if (btnStartIMU.getText().equals("Stop IMU")) {
                    mSensorManager.unregisterListener(MouseActivity.this);
                    btnStartIMU.setText("Start IMU");
                }


            }
        });


        findViewById(id.activity_mouse).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent motionEvent) {
                Log.d(TAG,"Touch Event");
                switch (motionEvent.getAction()) {
                    case ACTION_DOWN:
                    case ACTION_POINTER_DOWN:
                        maxPointerCount = motionEvent.getPointerCount();
                        X = motionEvent.getX();
                        Y = motionEvent.getY();
                        firstX = X;
                        firstY = Y;
                        return true;

                    case ACTION_MOVE:
                        maxPointerCount = Math.max(maxPointerCount, motionEvent.getPointerCount());
                        if (mouse != null) {
                            mouse.movePointer((int) (motionEvent.getX() - X), (int) (motionEvent.getY() - Y), 0, false, false, false);
                        }
                        X = motionEvent.getX();
                        Y = motionEvent.getY();
                        return true;

                    case ACTION_UP:
                    case ACTION_POINTER_UP:
                        X = motionEvent.getX();
                        Y = motionEvent.getY();
                        if ((X-firstX) * (X-firstX) + (Y-firstY) * (Y-firstY) < 20) {
                            if (mouse != null) {
                                if (maxPointerCount == 1) {
                                    mouse.movePointer((int) (motionEvent.getX() - X), (int) (motionEvent.getY() - Y), 0, true, false, false);
                                    mouse.movePointer((int) (motionEvent.getX() - X), (int) (motionEvent.getY() - Y), 0, false, false, false);
                                } else if (maxPointerCount == 2) {
                                    mouse.movePointer((int) (motionEvent.getX() - X), (int) (motionEvent.getY() - Y), 0, false, false, true);
                                    mouse.movePointer((int) (motionEvent.getX() - X), (int) (motionEvent.getY() - Y), 0, false, false, false);
                                } else if (maxPointerCount > 2) {
                                    mouse.movePointer((int) (motionEvent.getX() - X), (int) (motionEvent.getY() - Y), 0, false, true, false);
                                    mouse.movePointer((int) (motionEvent.getX() - X), (int) (motionEvent.getY() - Y), 0, false, false, false);
                                }
                            }
                        }
                        return true;
                }
                return false;
            }
        });

    }

    @Override
    void setupBlePeripheralProvider() {
        mouse = new MousePeripheral(this);
        mouse.setDeviceName(getString(string.ble_mouse));
        mouse.startAdvertising();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mouse != null) {
            mouse.stopAdvertising();
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.v(TAG,"Sensor event.");
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAccelerometerReading[0] = event.values[0];
            mAccelerometerReading[1] = event.values[1];
            mAccelerometerReading[2] = event.values[2];
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mMagnetometerReading[0] = event.values[0];
            mMagnetometerReading[1] = event.values[1];
            mMagnetometerReading[2] = event.values[2];
        }

        // Rotation matrix based on current readings from accelerometer and magnetometer.
        mSensorManager.getRotationMatrix(mRotationMatrix, null,
                mAccelerometerReading, mMagnetometerReading);

        mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

        // Conversion to Degrees
        mOrientationAngles[0] *= RAD_TO_DEGREE;
        mOrientationAngles[1] *= RAD_TO_DEGREE;
        mOrientationAngles[2] *= RAD_TO_DEGREE;




        eulerMove();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
