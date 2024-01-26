package com.astoev.cave.survey.service.bluetooth;

import static com.astoev.cave.survey.Constants.Measures.distance;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.EditText;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.Constants.Measures;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.map.MapUtilities;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Common code for screens using Bluetooth communication.
 */
public class BTMeasureResultReceiver extends ResultReceiver {

    private BTResultAware mTarget;
    private Set<Measures> mExpectedMeasures = new HashSet<>();
    private boolean useAdjustment;
    private float lengthAdjustmentValue;

    public BTMeasureResultReceiver(BTResultAware aTarget) {
        super(new Handler());
        mTarget = aTarget;
        useAdjustment = ConfigUtil.getBooleanProperty(ConfigUtil.PREF_MEASUREMENTS_ADJUSTMENT);
        if (useAdjustment) {
            lengthAdjustmentValue = ConfigUtil.getFloatProperty(ConfigUtil.PREF_MEASUREMENTS_ADJUSTMENT_VALUE);
            Log.d(Constants.LOG_TAG_BT, "Will use length adjustment by " + lengthAdjustmentValue);
        }
    }

    @Override
    public void onReceiveResult(int aResultCode, Bundle aResultData) {

        switch (aResultCode) {
            case Activity.RESULT_OK:

                if (aResultData.containsKey(Constants.MEASURE_METADATA_KEY)) {
                    String[] measureNames = aResultData.getStringArray(Constants.MEASURE_TYPE_KEY);
                    List<Measures> measures = Arrays.stream(measureNames).map(t -> Measures.valueOf(t)).collect(Collectors.toList());
                    if (measures.contains(Measures.distance) || measures.contains(Measures.angle)
                        || measures.contains(Measures.slope)) {
                        Log.i(Constants.LOG_TAG_SERVICE, "Persist new metadata");
                        Map<String, Object> metaData = (Map<String, Object>) aResultData.getSerializable(Constants.MEASURE_METADATA_KEY);
                        mTarget.onReceiveMetadata(metaData);
                    }
                } else {

                    // display received data
                    float[] measuresArray = aResultData.getFloatArray(Constants.MEASURE_VALUE_KEY);
                    String[] targetsArray = aResultData.getStringArray(Constants.MEASURE_TARGET_KEY);
                    // not yet used
    //                    String[] typesArray = aResultData.getStringArray(Constants.MEASURE_TYPE_KEY);
    //                    String[] unitsArray = aResultData.getStringArray(Constants.MEASURE_UNIT_KEY);


                    for (int i = 0; i < measuresArray.length; i++) {
                        Measures type = Measures.valueOf(targetsArray[i]);
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

                                // adjust if needed
                                if (useAdjustment) {
                                    measure += lengthAdjustmentValue;
                                }

                                // communication to Bluetooth devices in meters, convert to feet if needed
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
                }
                break;

            default:
                UIUtilities.showNotification(aResultData.getString("error"));
        }
    }

    public boolean expectsMeasure(Measures aMeasure) {
        return mExpectedMeasures.contains(aMeasure);
    }

    public void awaitMeasure(Measures aMeasure) {
        mExpectedMeasures.add(aMeasure);
    }

    public void awaitMeasures(Measures[] aMeasures) {
        if (aMeasures != null) {
            mExpectedMeasures.addAll(Arrays.asList(aMeasures));
        }
    }

    public void ignoreMeasure(Measures aMeasure) {
        mExpectedMeasures.remove(aMeasure);
    }

    public void resetMeasureExpectations() {
        mExpectedMeasures.clear();
        BluetoothService.cancelReadCommands();
    }

    public void bindBTMeasures(final EditText text, final Measures aMeasure, boolean aDirectSendCommandFlag, final Measures[] otherMeasuresWelcome) {

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
    }

    private void triggerBluetoothMeasure(Measures aMeasure, Measures[] otherMeasuresWelcome) {
        // register listeners & send command
        BluetoothService.sendReadMeasureCommand(this, aMeasure, otherMeasuresWelcome);
        Log.i(Constants.LOG_TAG_UI, "Command scheduled for " + aMeasure);
    }

    public void startScanning(EditText aDistanceField, EditText aAzimuthField, EditText aSlopeField) {

        // checks
        if (!BluetoothService.isBluetoothSupported() || !ensureDeviceSelected(false)) {
            Log.i(Constants.LOG_TAG_UI, "BT not ready");
        }


        if (!BluetoothService.isMeasureSupported(Constants.MeasureTypes.distance)) {
            Log.i(Constants.LOG_TAG_UI, "Distance measure not supported by device");
            return;
        }

        if (!Option.CODE_SENSOR_BLUETOOTH.equals(Options.getOptionValue(Option.CODE_AZIMUTH_SENSOR))
                || !BluetoothService.isMeasureSupported(Constants.MeasureTypes.angle)) {
            Log.i(Constants.LOG_TAG_UI, "Angle measure not supported by device");
            return;
        }

        if (!Option.CODE_SENSOR_BLUETOOTH.equals(Options.getOptionValue(Option.CODE_SLOPE_SENSOR))
                || !BluetoothService.isMeasureSupported(Constants.MeasureTypes.slope)) {
            Log.i(Constants.LOG_TAG_UI, "Slope measure not supported by device");
            return;
        }


        Log.i(Constants.LOG_TAG_UI, "Allow scanning");
        resetMeasureExpectations();
        awaitMeasure(distance);
        awaitMeasures(new Measures[] {Measures.angle, Measures.slope});
        BluetoothService.startScanning(this);
    }

    public BTResultAware getTarget() {
        return mTarget;
    }
}
