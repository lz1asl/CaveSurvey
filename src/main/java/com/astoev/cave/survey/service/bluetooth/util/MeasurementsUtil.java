package com.astoev.cave.survey.service.bluetooth.util;

import androidx.fragment.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.activity.dialog.AzimuthAndSlopeDialog;
import com.astoev.cave.survey.activity.dialog.AzimuthDialog;
import com.astoev.cave.survey.activity.dialog.SlopeDialog;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.util.ConfigUtil;

import static com.astoev.cave.survey.util.ConfigUtil.PREF_SENSOR_SIMULTANEOUSLY;

/**
 * Created by astoev on 4/20/15.
 */
public class MeasurementsUtil {

    private static final String AZIMUTH_DIALOG = "azimuth_dialog";
    private static final String SLOPE_DIALOG = "slope_dialog";
    private static final String AZIMUTH_SLOPE_DIALOG = "azimuth_slope_dialog";

    private static AzimuthDialog mAzimuthDialog = null;
    private static SlopeDialog mSlopeDialog = null;
    private static AzimuthAndSlopeDialog mAzimuthSlopeDialog = null;


    public static void bindSensorsAwareFields(final EditText azimuthBox, final EditText slopeBox, final FragmentManager aSupportFragmentManager) {
        bindSensorsAwareFields(azimuthBox, slopeBox, aSupportFragmentManager,
                Options.getOptionValue(Option.CODE_AZIMUTH_SENSOR), Options.getOptionValue(Option.CODE_SLOPE_SENSOR));
    }

    public static void bindSensorsAwareFields(final EditText anAzimuthBox, final EditText aSlopeBox, final FragmentManager aSupportFragmentManager,
                                              String azimuthSensor, String slopeSensor) {

        // combined azimuth and slope dialog
        if (anAzimuthBox != null && aSlopeBox != null && Option.CODE_SENSOR_INTERNAL.equals(azimuthSensor)
                && Option.CODE_SENSOR_INTERNAL.equals(slopeSensor) && ConfigUtil.getBooleanProperty(PREF_SENSOR_SIMULTANEOUSLY)) {
            Log.i(Constants.LOG_TAG_UI, "Will register combined sensors");
            View.OnClickListener dialogListener = new View.OnClickListener() {
                @Override
                public void onClick(View aView) {
                    if (mAzimuthSlopeDialog != null) {
                        mAzimuthSlopeDialog.dismiss();
                        mAzimuthSlopeDialog.cancelDialog();
                    }
                    mAzimuthSlopeDialog = new AzimuthAndSlopeDialog();
                    mAzimuthSlopeDialog.setTargetAzimuthTextBox(anAzimuthBox);
                    mAzimuthSlopeDialog.setTargetSlopeTextBox(aSlopeBox);
                    mAzimuthSlopeDialog.show(aSupportFragmentManager, AZIMUTH_SLOPE_DIALOG);
                }
            };
            anAzimuthBox.setOnClickListener(dialogListener);
            aSlopeBox.setOnClickListener(dialogListener);

            // ignore separate dialogs
            return;
        }

        // separate azimuth dialog
        if (anAzimuthBox != null && Option.CODE_SENSOR_INTERNAL.equals(azimuthSensor)) {
            Log.i(Constants.LOG_TAG_UI, "Will register onClickListener for Azimuth");
            anAzimuthBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAzimuthDialog != null) {
                        mAzimuthDialog.dismiss();
                        mAzimuthDialog.cancelDialog();
                    }
                    mAzimuthDialog = new AzimuthDialog();
                    mAzimuthDialog.setTargetAzimuthTextBox(anAzimuthBox);
                    mAzimuthDialog.show(aSupportFragmentManager, AZIMUTH_DIALOG);
                }
            });
        }

        // separate slope dialog
        if (aSlopeBox != null && Option.CODE_SENSOR_INTERNAL.equals(slopeSensor)) {
            Log.i(Constants.LOG_TAG_UI, "Will register onClickListener for Slope");
            aSlopeBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View viewArg) {
                    if (mSlopeDialog != null) {
                        mSlopeDialog.dismiss();
                        mSlopeDialog.cancelDialog();
                    }
                    mSlopeDialog = new SlopeDialog();
                    mSlopeDialog.setTargetSlopeTextBox(aSlopeBox);
                    mSlopeDialog.show(aSupportFragmentManager, SLOPE_DIALOG);
                }
            });
        }
    }




    public static void closeDialogs() {
        if (mAzimuthDialog != null) {
            mAzimuthDialog.cancelDialog();
        }
        if (mSlopeDialog != null) {
            mSlopeDialog.cancelDialog();
        }
        if (mAzimuthSlopeDialog != null) {
            mAzimuthSlopeDialog.cancelDialog();
        }
    }
}
