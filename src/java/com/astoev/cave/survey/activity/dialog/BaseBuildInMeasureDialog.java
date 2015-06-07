package com.astoev.cave.survey.activity.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.orientation.OrientationProcessor;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

/**
 * Created by astoev on 4/25/15.
 */
public class BaseBuildInMeasureDialog extends DialogFragment {


    /** Max value for the progress bar*/
    private static int PROGRESS_MAX_VALUE = 3;

    /** Formatter */
    protected DecimalFormat formater;

    /** Flag if the azimuth is expected in degrees */
    protected boolean isInDegrees = true;

    /** String for azimuth's units */
    protected String unitsString;

    protected ProgressBar progressBar;

    /** Progress thread*/
    protected ProgressThread progressThread;

    /** Progress handler*/
    protected ProgressHandler progressHandler;

    /** OrientationProcessor that handles the work with the sensors*/
    protected OrientationProcessor orientationProcessor;

    /** Last value for the slope from the sensor */
    protected float lastValue;

    protected EditText targetTextBox;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        formater = new DecimalFormat("#.#");

        if (Option.UNIT_DEGREES.equals(Options.getOptionValue(Option.CODE_AZIMUTH_UNITS))){
            isInDegrees = true;
            unitsString = " " + getString(R.string.degrees);
        } else {
            isInDegrees = false;
            unitsString = " " + getString(R.string.grads);
        }

        // create a handler and a thread that will drive the progress bar
        progressHandler = new ProgressHandler(this);
        progressThread = new ProgressThread(progressHandler);
        progressThread.start();

        return null;
    }

    public void setTargetTextBox(EditText aTargetTextBox) {
        this.targetTextBox = aTargetTextBox;
    }

    /**
     * Define the Handler that receives messages from the thread and update the progress
     *
     * @author astoev
     * @author jmitrev
     *
     */
    public static class ProgressHandler extends Handler {

        private WeakReference<BaseBuildInMeasureDialog> reference;

        public ProgressHandler(BaseBuildInMeasureDialog dialogFragmentArg)
        {
            reference = new WeakReference<BaseBuildInMeasureDialog>(dialogFragmentArg);
        }

        public void handleMessage(Message msg) {

            BaseBuildInMeasureDialog dialog = reference.get();
            if (dialog == null){
                return;
            }

            int total = msg.arg1;
            dialog.progressBar.setProgress(total);
            dialog.progressBar.setSecondaryProgress(total);
            dialog.progressBar.invalidate();
            if (total >= PROGRESS_MAX_VALUE) {
                dialog.progressThread.setState(ProgressThread.STATE_DONE);
                dialog.notifyEndProgress();
            }
        }
    }

    public static class BackKeyListener implements DialogInterface.OnKeyListener {

        private WeakReference<BaseBuildInMeasureDialog> reference;

        public BackKeyListener(BaseBuildInMeasureDialog dialogFragmentArg)
        {
            reference = new WeakReference<BaseBuildInMeasureDialog>(dialogFragmentArg);
        }

        @Override
        public boolean onKey(DialogInterface dialogArg, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {

                Log.i(Constants.LOG_TAG_UI, "Back button pressed! Cancel AzimuthDialog.");

                BaseBuildInMeasureDialog dialog= reference.get();
                if (dialog != null){
                    dialog.cancelDialog();
                }
            }
            return false;
        }
    }

    @Override
    public void onPause() {
        cancelDialog();

        super.onPause();
    }

    /**
     * Helper method that handles when the dialog is handled. It will stop the thread and stop the azimuth
     * processor
     */
    public void cancelDialog(){
        // stop the progress thread
        if (progressThread != null) {
            progressThread.setState(ProgressThread.STATE_DONE);
        }

        // stop azimuth listener
        if (orientationProcessor != null) {
            orientationProcessor.stopListening();
        }

        dismiss();
    }

    /**
     * Helper method called to notify that progress bar is filled and the dialog should be dismissed.
     * Stops the azimuth processor notifies the parent activity and will dismiss the dialog
     */
    protected void notifyEndProgress(){
        orientationProcessor.stopListening();

        targetTextBox.setText(String.valueOf(lastValue));

        dismiss();
    }

    /**
     * Nested class that performs progress calculations (counting)
     */
    protected class ProgressThread extends Thread {
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
            while (mState == STATE_RUNNING && total < PROGRESS_MAX_VALUE + 1) {
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

        /**
         * Sets the current state for the thread, used to stop the thread
         */
        public void setState(int state) {
            mState = state;
        }
    }// end of class ProgressThread

}
