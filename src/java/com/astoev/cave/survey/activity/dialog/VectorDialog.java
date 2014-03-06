package com.astoev.cave.survey.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.main.MainActivity;
import com.astoev.cave.survey.activity.main.PointActivity;
import com.astoev.cave.survey.activity.map.MapUtilities;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Vector;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.StringUtils;

import java.sql.SQLException;

/**
 * Created by astoev on 3/3/14.
 */
public class VectorDialog extends AzimuthDialog {

    private Leg mLeg;

    /**
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceStateArg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.main_add_vector));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.vector_dialog, null);
        builder.setView(view);

        final Dialog dialog = builder.create();

        Button addButton = (Button) view.findViewById(R.id.vector_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText distanceEdit = (EditText) view.findViewById(R.id.vector_distance);
                EditText azimuthEdit = (EditText) view.findViewById(R.id.vector_azimuth);
                EditText slopeEdit = (EditText) view.findViewById(R.id.vector_slope);

                boolean valid = true;
                valid = valid && UIUtilities.validateNumber(distanceEdit, true);
                valid = valid && UIUtilities.validateNumber(azimuthEdit, true) && UIUtilities.checkAzimuth(azimuthEdit);
                valid = valid && UIUtilities.validateNumber(slopeEdit, false) && UIUtilities.checkSlope(slopeEdit);

                if (!valid) {
                    return;
                }


                try {
                    Vector vector = new Vector();
                    vector.setDistance(StringUtils.getFromEditTextNotNull(distanceEdit));
                    vector.setAzimuth(StringUtils.getFromEditTextNotNull(azimuthEdit));
                    vector.setSlope(StringUtils.getFromEditTextNotNull(slopeEdit));
                    vector.setPoint(mLeg.getFromPoint());

                    DaoUtil.saveVector(vector);
                } catch (SQLException e) {
                    Log.e(Constants.LOG_TAG_UI, "Failed to add vector", e);
                    UIUtilities.showNotification(R.string.error);
                }

                Intent intent = new Intent(getActivity(), PointActivity.class);
                intent.putExtra(Constants.LEG_SELECTED, mLeg.getId());
                startActivity(intent);

                dialog.dismiss();
            }
        });

        return dialog;
    }

    public Leg getLeg() {
        return mLeg;
    }

    public void setLeg(Leg aLeg) {
        mLeg = aLeg;
    }
}
