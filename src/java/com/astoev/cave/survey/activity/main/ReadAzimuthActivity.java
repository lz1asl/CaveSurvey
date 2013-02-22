package com.astoev.cave.survey.activity.main;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 5/6/12
 * Time: 11:33 PM
 * To change this template use File | Settings | File Templates.
 */

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;

public class ReadAzimuthActivity extends Activity implements SensorEventListener {
    static final int PROGRESS_DIALOG = 0;
    Button button;
    ProgressThread progressThread;
    ProgressDialog progressDialog;

    private SensorManager mSensorManager;
    private Sensor mCompassSensor;

    private float mLastValue;

    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mSensorManager == null) {
            mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
            mCompassSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        mSensorManager.registerListener(this, mCompassSensor, SensorManager.SENSOR_DELAY_FASTEST);

        showDialog(PROGRESS_DIALOG);
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                progressDialog = new ProgressDialog(ReadAzimuthActivity.this);
//                progressDialog.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setMessage("Reading ...");
                return progressDialog;
            default:
                return null;
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case PROGRESS_DIALOG:

                progressDialog.setMax(3);
                progressDialog.setProgress(0);
                progressThread = new ProgressThread(handler);
                progressThread.start();
        }
    }

    // Define the Handler that receives messages from the thread and update the progress
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int total = msg.arg1;
            progressDialog.setProgress(total);
            progressDialog.setSecondaryProgress(total);
            if (total >= 3) {
                progressThread.setState(ProgressThread.STATE_DONE);
                dismissDialog(PROGRESS_DIALOG);


                mSensorManager.unregisterListener(ReadAzimuthActivity.this, mCompassSensor);

                Intent intent = ReadAzimuthActivity.this.getIntent();
                intent.putExtra("Azimuth", mLastValue);
                ReadAzimuthActivity.this.setResult(RESULT_OK, intent);

                finish();
            }
        }
    };

    /**
     * Nested class that performs progress calculations (counting)
     */
    private class ProgressThread extends Thread {
        Handler mHandler;
        final static int STATE_DONE = 0;
        final static int STATE_RUNNING = 1;
        int mState;
        int total;

        ProgressThread(Handler h) {
            mHandler = h;
        }

        public void run() {
            mState = STATE_RUNNING;
            total = 0;
            while (mState == STATE_RUNNING) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("ERROR", "Thread Interrupted");
                }
                Message msg = mHandler.obtainMessage();
                msg.arg1 = total;
                mHandler.sendMessage(msg);
                total++;
            }
        }

        /* sets the current state for the thread,
  * used to stop the thread */
        public void setState(int state) {
            mState = state;
        }
    }


    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this, mCompassSensor);
        super.onPause();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        mLastValue = event.values[0];
        // TODO add unit
        progressDialog.setMessage("aa " + String.valueOf(mLastValue));
    }
}
