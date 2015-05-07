package com.astoev.cave.survey.service.bluetooth.util;

import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.activity.dialog.AzimuthDialog;
import com.astoev.cave.survey.activity.dialog.SlopeDialog;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.orientation.AzimuthChangedListener;
import com.astoev.cave.survey.service.orientation.SlopeChangedListener;

/**
 * Created by astoev on 4/20/15.
 */
public class MeasurementsUtil {

    private static final String AZIMUTH_DIALOG = "azimuth_dialog";
    private static final String SLOPE_DIALOG = "slope_dialog";

    private static AzimuthDialog mAzimuthDialog = null;
    private static SlopeDialog mSlopeDialog = null;


    public static void bindAzimuthAwareField(final EditText anAzimuthBox, final FragmentManager aSupportFragmentManager) {
        // if the azimuth is read from build in sensors add onClickListener to show azimuth dialog
        if (Option.CODE_SENSOR_INTERNAL.equals(Options.getOptionValue(Option.CODE_AZIMUTH_SENSOR))) {
            Log.i(Constants.LOG_TAG_UI, "Will register onClickListener for Azimuth");
            anAzimuthBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAzimuthDialog != null) {
                        mAzimuthDialog.dismiss();
                        mAzimuthDialog.cancelDialog();
                    }
                    mAzimuthDialog = new AzimuthDialog();
                    mAzimuthDialog.setTargetTextBox(anAzimuthBox);
                    mAzimuthDialog.show(aSupportFragmentManager, AZIMUTH_DIALOG);
                }
            });
        }
    }

    public static void bindSlopeAwareField(final EditText anSlopeBox, final FragmentManager aSupportFragmentManager) {
        // if the slope is read from build in sensors add onClickListener to show slope dialog
        if (Option.CODE_SENSOR_INTERNAL.equals(Options.getOptionValue(Option.CODE_SLOPE_SENSOR))) {
            Log.i(Constants.LOG_TAG_UI, "Will register onClickListener for Slope");
            anSlopeBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View viewArg) {
                    if (mSlopeDialog != null) {
                        mSlopeDialog.dismiss();
                        mSlopeDialog.cancelDialog();
                    }
                    mSlopeDialog = new SlopeDialog();
                    mSlopeDialog.setTargetTextBox(anSlopeBox);
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
    }
}
