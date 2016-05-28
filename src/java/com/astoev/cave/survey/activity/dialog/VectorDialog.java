package com.astoev.cave.survey.activity.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.main.PointActivity;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Vector;
import com.astoev.cave.survey.service.bluetooth.BTMeasureResultReceiver;
import com.astoev.cave.survey.service.bluetooth.BTResultAware;
import com.astoev.cave.survey.service.bluetooth.util.MeasurementsUtil;
import com.astoev.cave.survey.service.orientation.AzimuthChangedListener;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.StringUtils;

import java.sql.SQLException;

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

    FragmentManager mSupportFragmentManager;

    public VectorDialog() {
    }

    @SuppressLint("ValidFragment")
    public VectorDialog(FragmentManager aSupportFragmentManager) {
        mSupportFragmentManager = aSupportFragmentManager;
    }

    /**
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceStateArg) {

        mLeg = getArguments() != null ? (Leg)getArguments().getSerializable(LEG) : null;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.main_add_vector));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.vector_dialog, null);
        mView = view;
        builder.setView(view);

        // Bluetooth registrations
        mReceiver = new BTMeasureResultReceiver(this);
        EditText distanceField = (EditText) mView.findViewById(R.id.vector_distance);
        mReceiver.bindBTMeasures(distanceField, Constants.Measures.distance, false, new Constants.Measures[] {Constants.Measures.angle, Constants.Measures.slope});

        EditText angleField = (EditText) mView.findViewById(R.id.vector_azimuth);
        mReceiver.bindBTMeasures(angleField, Constants.Measures.angle, false, new Constants.Measures[] {Constants.Measures.distance, Constants.Measures.slope});
        MeasurementsUtil.bindAzimuthAwareField(angleField, mSupportFragmentManager);

        EditText slopeField = (EditText) mView.findViewById(R.id.vector_slope);
        mReceiver.bindBTMeasures(slopeField, Constants.Measures.slope, false, new Constants.Measures[] {Constants.Measures.angle, Constants.Measures.distance});
        MeasurementsUtil.bindSlopeAwareField(slopeField, mSupportFragmentManager);

        final Dialog dialog = builder.create();

        Button addButton = (Button) view.findViewById(R.id.vector_add);
        addButton.setOnClickListener(new AddVectorListener(mView, dialog, false));
        Button addAgainButton = (Button) view.findViewById(R.id.vector_add_again);
        addAgainButton.setOnClickListener(new AddVectorListener(mView, dialog, true));

        return dialog;
    }

    @Override
    public void onReceiveMeasures(Constants.Measures aMeasureTarget, float aMeasureValue) {
        switch (aMeasureTarget) {
            case distance:
                Log.i(Constants.LOG_TAG_UI, "Got vector distance " + aMeasureValue);
                populateMeasure(aMeasureValue, R.id.vector_distance);
                break;

            case angle:
                Log.i(Constants.LOG_TAG_UI, "Got vector angle " + aMeasureValue);
                populateMeasure(aMeasureValue, R.id.vector_azimuth);
                break;

            case slope:
                Log.i(Constants.LOG_TAG_UI, "Got vector slope " + aMeasureValue);
                populateMeasure(aMeasureValue, R.id.vector_slope);
                break;

            default:
                Log.i(Constants.LOG_TAG_UI, "Ignore type " + aMeasureTarget);
        }
    }

    /**
     * @see com.astoev.cave.survey.service.orientation.AzimuthChangedListener#onAzimuthChanged(float)
     */
    @Override
    public void onAzimuthChanged(float newValueArg) {
        EditText angleField = (EditText) mView.findViewById(R.id.vector_azimuth);
        angleField.setText(String.valueOf(newValueArg));
    }

    private void populateMeasure(float aMeasure, int anEditTextId) {
        EditText field = (EditText) mView.findViewById(anEditTextId);
        StringUtils.setNotNull(field, aMeasure);
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

                // get values
                EditText distanceEdit = (EditText) mView.findViewById(R.id.vector_distance);
                EditText azimuthEdit = (EditText) mView.findViewById(R.id.vector_azimuth);
                EditText slopeEdit = (EditText) mView.findViewById(R.id.vector_slope);

                // validate
                boolean valid = true;
                valid = valid && UIUtilities.validateNumber(distanceEdit, true);
                valid = valid && UIUtilities.validateNumber(azimuthEdit, true) && UIUtilities.checkAzimuth(azimuthEdit);
                valid = valid && UIUtilities.validateNumber(slopeEdit, false) && UIUtilities.checkSlope(slopeEdit);

                if (!valid) {
                    return;
                }

                // persist
                try {
                    Vector vector = new Vector();
                    vector.setDistance(StringUtils.getFromEditTextNotNull(distanceEdit));
                    vector.setAzimuth(StringUtils.getFromEditTextNotNull(azimuthEdit));
                    vector.setSlope(StringUtils.getFromEditTextNotNull(slopeEdit));
                    vector.setPoint(mLeg.getFromPoint());
                    vector.setGalleryId(mLeg.getGalleryId());

                    DaoUtil.saveVector(vector);
                } catch (SQLException e) {
                    Log.e(Constants.LOG_TAG_UI, "Failed to add vector", e);
                    UIUtilities.showNotification(R.string.error);
                }

                // refresh parent
                Activity parent = getActivity();
                if (parent instanceof PointActivity) {
                    ((PointActivity) parent).loadLegVectors(mLeg);
                }

                if (mPrepareNewVector) {
                    // notify saved
                    Log.i(Constants.LOG_TAG_UI, "Vector saved, cleaning");
                    UIUtilities.showNotification(R.string.action_saved);

                    // reset values and stay here
                    distanceEdit.setText(null);
                    azimuthEdit.setText(null);
                    slopeEdit.setText(null);
                } else {
                    // go back
                    Log.i(Constants.LOG_TAG_UI, "Vector saved, going back");
                    mDialog.dismiss();
                }
            }
    }
}
