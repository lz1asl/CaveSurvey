package com.astoev.cave.survey.activity.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.orientation.AzimuthChangedAdapter;
import com.astoev.cave.survey.service.orientation.MeasurementsFilter;
import com.astoev.cave.survey.service.orientation.OrientationProcessor;
import com.astoev.cave.survey.service.orientation.OrientationProcessorFactory;
import com.astoev.cave.survey.service.orientation.SlopeChangedAdapter;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.PermissionUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static android.Manifest.permission.CAMERA;
import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.astoev.cave.survey.Constants.LOG_TAG_SERVICE;
import static com.astoev.cave.survey.model.Option.MAX_VALUE_SLOPE_DEGREES;
import static com.astoev.cave.survey.util.ConfigUtil.PREF_DEVICE_HEADING_CAMERA;
import static com.astoev.cave.survey.util.ConfigUtil.PREF_SENSOR_TIMEOUT;
import static java.lang.Thread.sleep;

/**
 * Created by astoev on 4/25/15.
 */
public class BaseBuildInMeasureDialog extends DialogFragment implements SurfaceHolder.Callback {


    /** Max value for the progress bar*/
    protected static int progressMaxValue;
    public static int PROGRESS_DEFAULT_VALUE = 3;
    private static final int PERM_REQ_CODE_CAMERA = 101;


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

    /** OrientationProcessors that handle the work with the sensors*/
    protected OrientationProcessor orientationAzimuthProcessor;
    protected OrientationProcessor orientationSlopeProcessor;

    /** Last value for the slope from the sensor */
    protected MeasurementsFilter azimuthFilter;
    protected MeasurementsFilter slopeFilter;

    protected EditText targetAzimuthTextBox;
    protected EditText targetSlopeTextBox;

    protected int lastAzimuthAccuracy;
    protected int lastSlopeAccuracy;

    // camera preview
    private boolean cameraMode = false;
    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private boolean isCameraViewOn = false;
    private SurfaceView cameraPreview;
    private CameraDialogOverlay cameraOverlay;

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

        Integer userMaxProgressValue = ConfigUtil.getIntProperty(PREF_SENSOR_TIMEOUT);
        if (userMaxProgressValue == null) {
            progressMaxValue = PROGRESS_DEFAULT_VALUE;
        } else {
            progressMaxValue = userMaxProgressValue;
        }

        azimuthFilter = new MeasurementsFilter();
        azimuthFilter.initializeFromConfig();
        slopeFilter = new MeasurementsFilter();
        slopeFilter.initializeFromConfig();

        // create a handler and a thread that will drive the progress bar
        progressHandler = new ProgressHandler(this);
        progressThread = new ProgressThread(progressHandler);
        progressThread.start();

        return null;
    }

    protected void initCameraPreview(View aView) {
        cameraMode = ConfigUtil.getBooleanProperty(PREF_DEVICE_HEADING_CAMERA);


        if (cameraMode) {

            if (!PermissionUtil.requestPermission(CAMERA, this.getActivity(), PERM_REQ_CODE_CAMERA)) {
                return;
            }

            cameraPreview = aView.findViewById(R.id.cameraPreview);

            // scale the preview TODO
            DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
            float scaleFactor = 0.5f;
            surfaceHolder = cameraPreview.getHolder();
            surfaceHolder.addCallback(this);
            surfaceHolder.setFixedSize((int) (displayMetrics.widthPixels * scaleFactor),  (int) (displayMetrics.heightPixels * scaleFactor));
            cameraPreview.setVisibility(VISIBLE);

            cameraOverlay = aView.findViewById(R.id.cameraOverlay);
            cameraOverlay.setCameraMode(true);
            cameraOverlay.setVisibility(VISIBLE);
        }
    }

    public void setTargetAzimuthTextBox(EditText aTargetAzimuthTextBox) {
        targetAzimuthTextBox = aTargetAzimuthTextBox;
    }

    public void setTargetSlopeTextBox(EditText aTargetSlopeTextBox) {
        targetSlopeTextBox = aTargetSlopeTextBox;
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

        public ProgressHandler(BaseBuildInMeasureDialog dialogFragmentArg) {
            reference = new WeakReference<>(dialogFragmentArg);
        }

        public void handleMessage(Message msg) {

            BaseBuildInMeasureDialog dialog = reference.get();
            if (dialog == null) {
                return;
            }

            int total = msg.arg1;
            dialog.progressBar.setProgress(total);
            dialog.progressBar.setSecondaryProgress(total);
            dialog.progressBar.invalidate();
            if (total >= progressMaxValue) {
                dialog.progressThread.setState(ProgressThread.STATE_DONE);
                dialog.notifyEndProgress();
            }
        }
    }

    public static class BackKeyListener implements DialogInterface.OnKeyListener {

        private WeakReference<BaseBuildInMeasureDialog> reference;

        public BackKeyListener(BaseBuildInMeasureDialog dialogFragmentArg)
        {
            reference = new WeakReference<>(dialogFragmentArg);
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
    public void cancelDialog() {
        // stop the progress thread
        if (progressThread != null) {
            progressThread.setState(ProgressThread.STATE_DONE);
        }

        // stop azimuth listener
        if (orientationAzimuthProcessor != null) {
            orientationAzimuthProcessor.stopListening();
        }

        // stop slope listener
        if (orientationSlopeProcessor != null) {
            orientationSlopeProcessor.stopListening();
        }

        azimuthFilter = null;
        slopeFilter = null;

        dismiss();
    }

    /**
     * Helper method called to notify that progress bar is filled and the dialog should be dismissed.
     * Stops the azimuth processor notifies the parent activity and will dismiss the dialog
     */
    protected void notifyEndProgress() {
        Log.i(LOG_TAG_SERVICE, "End of targeting time");

        // start post processing
        if (azimuthFilter != null && slopeFilter != null) {
            azimuthFilter.startAveraging();
            slopeFilter.startAveraging();

            // await results and stop processors
            ExecutorService executor = Executors.newFixedThreadPool(3);
            executor.submit(new FutureTask(new AwaitFilterRunnable(orientationAzimuthProcessor, azimuthFilter, targetAzimuthTextBox, false), null));
            executor.submit(new FutureTask(new AwaitFilterRunnable(orientationSlopeProcessor, slopeFilter, targetSlopeTextBox, true), null));
            try {
                sleep(100);
                executor.shutdown();
            } catch (Exception e) {
                Log.e(LOG_TAG_SERVICE, "Interrupted", e);
            }
        }

        // dismiss the dialog
        dismiss();
    }

    class AwaitFilterRunnable implements Runnable {

        public AwaitFilterRunnable(OrientationProcessor processor, MeasurementsFilter filter, EditText targetTextBox, boolean isSlope) {
            this.processor = processor;
            this.filter = filter;
            this.targetTextBox = targetTextBox;
            this.isSlope = isSlope;
        }

        OrientationProcessor processor;
        MeasurementsFilter filter;
        EditText targetTextBox;
        boolean isSlope;

        @Override
        public void run() {
            if (processor != null) {
                // await averaging
                for (int i = 0; i < 60; i++) {
                    try {
                        Thread.currentThread().sleep(50);
                    } catch (InterruptedException e) {
                        Log.e(LOG_TAG_SERVICE, "interrupted", e);
                    }

                    if (filter.isReady()) {
                        Log.i(LOG_TAG_SERVICE, "Stop azimuth processor");
                        processor.stopListening();
                        // averaged value
                        // TODO the value is without camera offset and proper units


                        float value = filter.getValue();
                        value = postProcessSensorValue(value, isSlope);

                        targetTextBox.setText(String.valueOf(value));
                        return;
                    }
                }

                Log.e(LOG_TAG_SERVICE, "Averaging not ready");
            }
        }
    }



    /**
     * Nested class that performs progress calculations (counting)
     */
    protected static class ProgressThread extends Thread {
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
            while (mState == STATE_RUNNING && total < progressMaxValue + 1) {
                try {
                    sleep(1000);
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

    public static void setProgressMaxValue(int aProgressMaxValue) {
        progressMaxValue = aProgressMaxValue;
    }


    protected void startAzimuthProcessor(final TextView aAzimuthView, final TextView aAccuracyView) {
        // create azimuth processor to handle the azimuth sensors and value changes
        orientationAzimuthProcessor = OrientationProcessorFactory.getOrientationProcessor(getActivity(), new AzimuthChangedAdapter() {

            /**
             * Azimuth callback method. Edits the azimuth text view with the new value
             *
             * @see com.astoev.cave.survey.service.orientation.AzimuthChangedListener#onAzimuthChanged(float)
             */
            @Override
            public void onAzimuthChanged(float newValueArg) {
                //convert to Grads if necessary
                azimuthFilter.addMeasurement(newValueArg);
                float processedValue = azimuthFilter.getValue();
                processedValue = postProcessSensorValue(processedValue, false);

                aAzimuthView.setText(formater.format(processedValue) + unitsString);
                aAccuracyView.setText(orientationAzimuthProcessor.getAccuracyAsString(lastAzimuthAccuracy) + azimuthFilter.getAccuracyString());
            }

            /**
             * @see com.astoev.cave.survey.service.orientation.AzimuthChangedListener#onAzimuthChanged(float)
             */
            @Override
            public void onAccuracyChanged(int accuracyArg) {
                lastAzimuthAccuracy = accuracyArg;
                aAccuracyView.setText(orientationAzimuthProcessor.getAccuracyAsString(accuracyArg) + azimuthFilter.getAccuracyString());
            }
        });

        orientationAzimuthProcessor.startListening();
    }


    protected void startSlopeProcessor(final TextView aSlopeView, final TextView aSlopeAccuracyView) {
        // create azimuth processor to handle the azimuth sensors and value changes
        orientationSlopeProcessor = OrientationProcessorFactory.getOrientationProcessor(getActivity(), new SlopeChangedAdapter() {

            /**
             * Slope callback method. Edits the slope text view with the new value
             *
             * @see com.astoev.cave.survey.service.orientation.SlopeChangedAdapter#onSlopeChanged(float)
             */
            @Override
            public void onSlopeChanged(float newValueArg) {

                slopeFilter.addMeasurement(newValueArg);

                float processedValue = slopeFilter.getValue();
                processedValue = postProcessSensorValue(processedValue, true);

                aSlopeView.setText(formater.format(processedValue) + unitsString);
                aSlopeAccuracyView.setText(orientationSlopeProcessor.getAccuracyAsString(lastSlopeAccuracy) + slopeFilter.getAccuracyString());
            }

            /**
             * @see com.astoev.cave.survey.service.orientation.AzimuthChangedListener#onAzimuthChanged(float)
             */
            @Override
            public void onAccuracyChanged(int accuracyArg) {
                lastSlopeAccuracy = accuracyArg;
                aSlopeAccuracyView.setText(orientationSlopeProcessor.getAccuracyAsString(accuracyArg) + slopeFilter.getAccuracyString());
            }
        });

        orientationSlopeProcessor.startListening();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (cameraMode) {
            if (isCameraViewOn) {
                camera.stopPreview();
                isCameraViewOn = false;
            }

            if (camera != null) {
                try {
                    camera.setPreviewDisplay(surfaceHolder);
                    camera.startPreview();
                    isCameraViewOn = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (cameraMode) {

            camera = Camera.open();
            camera.setDisplayOrientation(90);

            Camera.Parameters params = camera.getParameters();
            // TODO size ?

            //set camera to continually auto-focus
            if (params.getSupportedFocusModes().contains(FOCUS_MODE_CONTINUOUS_VIDEO)) {
                params.setFocusMode(FOCUS_MODE_CONTINUOUS_VIDEO);
            }

            // zoom
            // TODO
//            params.setZoom(2);

            camera.setParameters(params);
        }
    }

    private float postProcessSensorValue(float value, boolean isSlope) {

        float processedValue = value;

        if (isSlope && cameraMode) {
            // phone upwards
            processedValue -= MAX_VALUE_SLOPE_DEGREES;
        }

        //convert to Grads if necessary
        if (!isInDegrees){
            processedValue = processedValue * Constants.DEC_TO_GRAD;
        }

        return processedValue;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        if (cameraMode) {
            if (camera != null) {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
            isCameraViewOn = false;
            cameraPreview.setVisibility(GONE);
        }
    }
}
