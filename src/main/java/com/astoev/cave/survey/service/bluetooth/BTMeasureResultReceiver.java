package com.astoev.cave.survey.service.bluetooth;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.EditText;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.map.MapUtilities;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Common code for screens using Bluetooth communication.
 */
public class BTMeasureResultReceiver extends ResultReceiver {

    private BTResultAware mTarget;
    private Set<Constants.Measures> mExpectedMeasures = new HashSet<>();

    public BTMeasureResultReceiver(BTResultAware aTarget) {
        super(new Handler());
        mTarget = aTarget;
    }

    @Override
    protected void onReceiveResult(int aResultCode, Bundle aResultData) {

        switch (aResultCode) {
            case Activity.RESULT_OK:


                float[] measuresArray = aResultData.getFloatArray(Constants.MEASURE_VALUE_KEY);
                String[] targetsArray = aResultData.getStringArray(Constants.MEASURE_TARGET_KEY);
                // not yet used
//                    String[] typesArray = aResultData.getStringArray(Constants.MEASURE_TYPE_KEY);
//                    String[] unitsArray = aResultData.getStringArray(Constants.MEASURE_UNIT_KEY);


                for (int i = 0; i < measuresArray.length; i++) {
                    Constants.Measures type = Constants.Measures.valueOf(targetsArray[i]);
                    if (!expectsMeasure(type)) {
                        Log.i(Constants.LOG_TAG_SERVICE, "Unexpected measure " + type + " : " + type);
                        return;
                    }

                    float measure = measuresArray[i];

                    switch (type) {
                        case up:
                        case down:
                        case left:
                        case right:
                        case distance:
                            // communicaiton to Bluetooth devices in meters, convert to feet if needed
                            if (Option.UNIT_FEET.equals(Options.getOptionValue(Option.CODE_DISTANCE_UNITS))) {
                                measure = MapUtilities.getMetersInFeet(measure);
                            }
                            break;

                        default:
                            // no conversion needed
                    }

                    // screen specific population of the measures
                    mTarget.onReceiveMeasures(type, measure);
                }
                break;

            default:
                UIUtilities.showNotification(aResultData.getString("error"));
        }


    }

    public boolean expectsMeasure(Constants.Measures aMeasure) {
        return mExpectedMeasures.contains(aMeasure);
    }

    public void awaitMeasure(Constants.Measures aMeasure) {
        mExpectedMeasures.add(aMeasure);
    }

    public void awaitMeasures(Constants.Measures[] aMeasures) {
        if (aMeasures != null) {
            mExpectedMeasures.addAll(Arrays.asList(aMeasures));
        }
    }

    public void ignoreMeasure(Constants.Measures aMeasure) {
        mExpectedMeasures.remove(aMeasure);
    }

    public void resetMeasureExpectations() {
        mExpectedMeasures.clear();
        BluetoothService.cancelReadCommands();
    }

    public void bindBTMeasures(final EditText text, final Constants.Measures aMeasure, boolean aDirectSendCommandFlag, final Constants.Measures[] otherMeasuresWelcome) {

        if (BluetoothService.isBluetoothSupported()) {

            if (!ensureDeviceSelected(false)) {
                Log.i(Constants.LOG_TAG_UI, "No device");
                return;
            }

            Log.d(Constants.LOG_TAG_UI, "Register field " + aMeasure + "?");
            switch (aMeasure) {
                case distance:
                case up:
                case down:
                case left:
                case right:
                    if (!Option.CODE_SENSOR_BLUETOOTH.equals(Options.getOptionValue(Option.CODE_DISTANCE_SENSOR))) {
                        return;
                    }

                    if (!BluetoothService.isMeasureSupported(Constants.MeasureTypes.distance)) {
                        Log.i(Constants.LOG_TAG_UI, "Distance measure not supported by device");
                        return;
                    }
                    break;

                case angle:
                    if (!Option.CODE_SENSOR_BLUETOOTH.equals(Options.getOptionValue(Option.CODE_AZIMUTH_SENSOR))) {
                        return;
                    }

                    if (!BluetoothService.isMeasureSupported(Constants.MeasureTypes.angle)) {
                        Log.i(Constants.LOG_TAG_UI, "Angle measure not supported by device");
                        return;
                    }
                    break;

                case slope:
                    if (!Option.CODE_SENSOR_BLUETOOTH.equals(Options.getOptionValue(Option.CODE_SLOPE_SENSOR))) {
                        return;
                    }

                    if (!BluetoothService.isMeasureSupported(Constants.MeasureTypes.slope)) {
                        Log.i(Constants.LOG_TAG_UI, "Slope measure not supported by device");
                        return;
                    }

                    break;
            }

            if (aDirectSendCommandFlag) {
                Log.i(Constants.LOG_TAG_UI, "Send read command early");
                resetMeasureExpectations();
                awaitMeasure(aMeasure);
                awaitMeasures(otherMeasuresWelcome);
                triggerBluetoothMeasure(aMeasure, otherMeasuresWelcome);
            } else {
                // supported for the measure, add the listener
                if (StringUtils.isEmpty(text)) {
                    // no current falue, just moving focus to the cell requests measure from BT
                    Log.i(Constants.LOG_TAG_UI, "Add BT focus listener");
                    text.setOnFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus) {
                            Log.i(Constants.LOG_TAG_UI, "Send read command for empty field");
                            resetMeasureExpectations();
                            awaitMeasure(aMeasure);
                            awaitMeasures(otherMeasuresWelcome);
                            triggerBluetoothMeasure(aMeasure, otherMeasuresWelcome);
                        } else {
                            ignoreMeasure(aMeasure);
                        }
                    });
                } else {
                    // trigger BT read only if you tap twice
                    Log.i(Constants.LOG_TAG_UI, "Add BT click listener");
                    text.setOnClickListener(v -> {
                        Log.i(Constants.LOG_TAG_UI, "Send read command, tapped twice");
                        resetMeasureExpectations();
                        awaitMeasure(aMeasure);
                        awaitMeasures(otherMeasuresWelcome);
                        triggerBluetoothMeasure(aMeasure, otherMeasuresWelcome);
                    });
                }
            }
        }
    }

    private boolean ensureDeviceSelected(boolean showBTOptions) {
        return BluetoothService.isDeviceSelected();

       /* if (showBTOptions) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mParentView.getContext());
            dialogBuilder.setMessage(R.string.bt_not_selected)
                    .setCancelable(false)
                    .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(mParentActivity, BTActivity.class);
                            mParentActivity.startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = dialogBuilder.create();
            alert.show();
        }*/
    }

    private void triggerBluetoothMeasure(Constants.Measures aMeasure, Constants.Measures[] otherMeasuresWelcome) {
        // register listeners & send command
        BluetoothService.sendReadMeasureCommand(this, aMeasure, otherMeasuresWelcome);
        Log.i(Constants.LOG_TAG_UI, "Command scheduled for " + aMeasure);
    }
}
