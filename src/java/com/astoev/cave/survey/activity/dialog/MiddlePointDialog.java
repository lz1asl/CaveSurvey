package com.astoev.cave.survey.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.main.PointActivity;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.service.bluetooth.BTMeasureResultReceiver;
import com.astoev.cave.survey.service.bluetooth.BTResultAware;
import com.astoev.cave.survey.util.StringUtils;
import com.j256.ormlite.misc.TransactionManager;

import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Dialog for chosing middle point length.
 * Created by astoev on 2/8/14.
 */
public class MiddlePointDialog extends DialogFragment implements BTResultAware {

    BTMeasureResultReceiver mReceiver;
    View view;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.middle_leg_add));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.middle_point_dialog, null);
        builder.setView(view);

        try {
            final Leg currLeg = Workspace.getCurrentInstance().getActiveLeg();
            TextView label = (TextView) view.findViewById(R.id.middleLegInfo);
            CharSequence distanceUnitsLabel = StringUtils.extractDynamicResource(
                    getResources(), StringUtils.RESOURCE_PREFIX_UNITS + Options.getOptionValue(Option.CODE_DISTANCE_UNITS));
            label.setText(getString(
                    R.string.middle_leg_info,
                    currLeg.buildLegDescription(),
                    StringUtils.floatToLabel(currLeg.getDistance()),
                    distanceUnitsLabel));
        } catch (SQLException e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to add middle point", e);
            UIUtilities.showNotification(R.string.error);
        }

        // Bluetooth registrations
        mReceiver = new BTMeasureResultReceiver(this);
        EditText distanceField = (EditText) view.findViewById(R.id.middle_distance);
        mReceiver.bindBTMeasures(distanceField, Constants.Measures.distance, true);

        final Dialog dialog = builder.create();

        Button createButton = (Button) view.findViewById(R.id.middle_create);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    EditText distanceInput = (EditText) view.findViewById(R.id.middle_distance);
                    Float distance = StringUtils.getFromEditTextNotNull(distanceInput);
                    if (null == distance) {
                        distanceInput.setError(getString(R.string.middle_no_value));
                        return;
                    }

                    if (distance.floatValue() <= 0) {
                        distanceInput.setError(getString(R.string.middle_leg_shorter));
                        return;
                    }

                    final Leg currLeg = Workspace.getCurrentInstance().getActiveLeg();

                    if (currLeg.getDistance().floatValue() <= distance.floatValue()) {
                        distanceInput.setError(getString(R.string.middle_leg_bigger));
                        return;
                    }

                    //  update DB
                    Leg theNewLeg = addMiddle(currLeg, distance.floatValue());

                    // show PointActivity
                    Intent intent = new Intent(view.getContext(), PointActivity.class);
                    intent.putExtra(Constants.LEG_SELECTED, theNewLeg.getId());
                    Workspace.getCurrentInstance().setActiveLeg(theNewLeg);
                    startActivity(intent);

                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG_UI, "Failed to add middle point", e);
                    UIUtilities.showNotification(R.string.error);
                }
                dialog.dismiss();
            }
        });

        return dialog;
    }


    private Leg addMiddle(final Leg aCurrentLeg, final float atDistance) throws SQLException {

        Log.i(Constants.LOG_TAG_UI, "Creating middle point");
        return TransactionManager.callInTransaction(Workspace.getCurrentInstance().getDBHelper().getConnectionSource(),
                new Callable<Leg>() {
                    public Leg call() throws Exception {
                        try {

                            // copy the leg
                            Leg newLeg = new Leg(aCurrentLeg.getFromPoint(), aCurrentLeg.getToPoint(), aCurrentLeg.getProject(), aCurrentLeg.getGalleryId());
                            newLeg.setMiddlePointDistance(atDistance);

                            newLeg.setAzimuth(aCurrentLeg.getAzimuth());
                            newLeg.setDistance(aCurrentLeg.getDistance());
                            newLeg.setSlope(aCurrentLeg.getSlope());
                            Workspace.getCurrentInstance().getDBHelper().getLegDao().create(newLeg);

                            return newLeg;
                        } catch (Exception e) {
                            Log.e(Constants.LOG_TAG_DB, "Failed to add middle point", e);
                            throw e;
                        }
                    }
                }
        );
    }

    @Override
    public void onReceiveMeasures(Constants.Measures aMeasureTarget, float aMeasureValue) {
        switch (aMeasureTarget) {
            case distance:
                Log.i(Constants.LOG_TAG_UI, "Got middle distance " + aMeasureValue);
                populateMeasure(aMeasureValue, R.id.middle_distance);
                break;

            default:
                Log.i(Constants.LOG_TAG_UI, "Ignore type " + aMeasureTarget);
        }
    }

    private void populateMeasure(float aMeasure, int anEditTextId) {
        EditText field = (EditText) view.findViewById(anEditTextId);
        StringUtils.setNotNull(field, aMeasure);
    }
}
