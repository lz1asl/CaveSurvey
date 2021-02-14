package com.astoev.cave.survey.activity.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.main.PointActivity;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Vector;
import com.astoev.cave.survey.service.bluetooth.BTMeasureResultReceiver;
import com.astoev.cave.survey.service.bluetooth.BTResultAware;
import com.astoev.cave.survey.service.bluetooth.BluetoothService;
import com.astoev.cave.survey.service.bluetooth.util.MeasurementsUtil;
import com.astoev.cave.survey.service.orientation.AzimuthChangedListener;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.StringUtils;

import java.sql.SQLException;

import static com.astoev.cave.survey.activity.dialog.VectorsModeDialog.MODE_SAVE_ON_RECEIVE;
import static com.astoev.cave.survey.activity.dialog.VectorsModeDialog.MODE_SCAN;

/**
 *
 * @author Alexander Stoev
 * @author Zhivko Mitrev
 */
public class VectorDialog extends DialogFragment implements BTResultAware, AzimuthChangedListener {

    public static final String LEG = "leg";

    private BTMeasureResultReceiver mReceiver;
    private Leg mLeg;
    private View mView;

    private FragmentManager mSupportFragmentManager;

    // fields
    private EditText mDistanceField;
    private EditText mAzimuthField;
    private EditText mSlopeField;

    private AddVectorListener saveButtonListener;

    private int mode;

    public VectorDialog() {
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mReceiver.resetMeasureExpectations();
    }

    @SuppressLint("ValidFragment")
    public VectorDialog(FragmentManager aSupportFragmentManager) {
        mSupportFragmentManager = aSupportFragmentManager;
    }

    /**
     * @see DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceStateArg) {

        mode = ConfigUtil.getIntProperty(ConfigUtil.PREF_VECTORS_MODE, VectorsModeDialog.MODE_SINGLE);
        if (MODE_SCAN == mode) {
            Log.i(Constants.LOG_TAG_BT, "Start scanning mode");
            BluetoothService.startScanning();
        }

        mLeg = getArguments() != null ? (Leg)getArguments().getSerializable(LEG) : null;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        int vectorsCount = refreshVectors();
        builder.setTitle(buildTitle(vectorsCount));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.vector_dialog, null);
        mView = view;
        builder.setView(view);

        // Bluetooth registrations
        mReceiver = new BTMeasureResultReceiver(this);
        mDistanceField = mView.findViewById(R.id.vector_distance);
        mReceiver.bindBTMeasures(mDistanceField, Constants.Measures.distance, false, new Constants.Measures[] {Constants.Measures.angle, Constants.Measures.slope});

        mAzimuthField = mView.findViewById(R.id.vector_azimuth);
        mReceiver.bindBTMeasures(mAzimuthField, Constants.Measures.angle, false, new Constants.Measures[] {Constants.Measures.distance, Constants.Measures.slope});

        mSlopeField = mView.findViewById(R.id.vector_slope);
        mReceiver.bindBTMeasures(mSlopeField, Constants.Measures.slope, false, new Constants.Measures[] {Constants.Measures.angle, Constants.Measures.distance});

        MeasurementsUtil.bindSensorsAwareFields(mAzimuthField, mSlopeField, mSupportFragmentManager);

        final Dialog dialog = builder.create();

        Button addButton = view.findViewById(R.id.vector_add);
        addButton.setOnClickListener(new AddVectorListener(mView, dialog, false));
        Button addAgainButton = view.findViewById(R.id.vector_add_again);
        saveButtonListener = new AddVectorListener(mView, dialog, true);
        addAgainButton.setOnClickListener(saveButtonListener);

        return dialog;
    }

    private String buildTitle(int aVectorsCount) {
        return getString(R.string.main_add_vector) + " " + (aVectorsCount + 1);
    }

    @Override
    public void onReceiveMeasures(Constants.Measures aMeasureTarget, float aMeasureValue) {
        // populate
        switch (aMeasureTarget) {
            case distance:
                Log.i(Constants.LOG_TAG_UI, "Got vector distance " + aMeasureValue);
                populateMeasure(aMeasureValue, mDistanceField);
                break;

            case angle:
                Log.i(Constants.LOG_TAG_UI, "Got vector angle " + aMeasureValue);
                populateMeasure(aMeasureValue, mAzimuthField);
                break;

            case slope:
                Log.i(Constants.LOG_TAG_UI, "Got vector slope " + aMeasureValue);
                populateMeasure(aMeasureValue, mSlopeField);
                break;

            default:
                Log.i(Constants.LOG_TAG_UI, "Ignore type " + aMeasureTarget);
        }

        // configured to continue
        if (MODE_SCAN == mode || MODE_SAVE_ON_RECEIVE == mode) {

            // if last measurement for the vector try to save
            if (!StringUtils.isEmpty(mDistanceField)
                    && !StringUtils.isEmpty(mAzimuthField) && !StringUtils.isEmpty(mSlopeField)) {
                Log.i(Constants.LOG_TAG_SERVICE, "Auto save vector");
                saveButtonListener.onClick(null);
            }
        }
    }

    /**
     * @see com.astoev.cave.survey.service.orientation.AzimuthChangedListener#onAzimuthChanged(float)
     */
    @Override
    public void onAzimuthChanged(float newValueArg) {
        mAzimuthField.setText(String.valueOf(newValueArg));
    }

    private void populateMeasure(float aMeasure, EditText aField) {
        StringUtils.setNotNull(aField, aMeasure);
        mAzimuthField.invalidate();
    }

    private class AddVectorListener implements View.OnClickListener {

        private View mView;
        private Dialog mDialog;
        private boolean mPrepareNewVector;

        public AddVectorListener(View aView, Dialog aDialog, boolean aPrepareNewVector) {
            mView = aView;
            mDialog = aDialog;
            mPrepareNewVector = aPrepareNewVector;
        }

        @Override
        public void onClick(View v) {

            // validate
            boolean valid = true;
            valid = valid && UIUtilities.validateNumber(mDistanceField, true);
            valid = valid && UIUtilities.validateNumber(mAzimuthField, true) && UIUtilities.checkAzimuth(mAzimuthField);
            valid = valid && UIUtilities.validateNumber(mSlopeField, false) && UIUtilities.checkSlope(mSlopeField);

            if (!valid) {
                return;
            }

            // persist
            try {
                Vector vector = new Vector();
                vector.setDistance(StringUtils.getFromEditTextNotNull(mDistanceField));
                vector.setAzimuth(StringUtils.getFromEditTextNotNull(mAzimuthField));
                vector.setSlope(StringUtils.getFromEditTextNotNull(mSlopeField));
                vector.setPoint(mLeg.getFromPoint());
                vector.setGalleryId(mLeg.getGalleryId());

                DaoUtil.saveVector(vector);
            } catch (SQLException e) {
                Log.e(Constants.LOG_TAG_UI, "Failed to add vector", e);
                UIUtilities.showNotification(R.string.error);
            }

            // refresh parent
            int numVectors = refreshVectors();

            if (mPrepareNewVector) {
                // notify saved
                Log.i(Constants.LOG_TAG_UI, "Vector saved, cleaning");
                UIUtilities.showNotification(R.string.action_saved);

                // reset values and stay here
                mDistanceField.setText(null);
                mAzimuthField.setText(null);
                mSlopeField.setText(null);
                mDialog.setTitle(buildTitle(numVectors));
            } else {
                // go back
                Log.i(Constants.LOG_TAG_UI, "Vector saved, going back");
                mDialog.dismiss();
            }
        }
    }

    private int refreshVectors() {
        Activity parent = getActivity();
        if (parent instanceof PointActivity) {
            return ((PointActivity) parent).loadLegVectors(mLeg);
        }
        return 0;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (MODE_SCAN == mode) {
            Log.i(Constants.LOG_TAG_UI, "Stop scanning");
            BluetoothService.stopScanning();
        }
    }
}
