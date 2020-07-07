package com.astoev.cave.survey.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.astoev.cave.survey.R;

public class AzimuthAndSlopeDialog extends BaseBuildInMeasureDialog {

    /**
     * @see DialogFragment#onCreateDialog(Bundle)
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // parent initialization
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.azimuth) + "/" + getString(R.string.slope));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.azimuth_slope_dialog, null);
        builder.setView(view);

        // add key listener to handle the back button
        builder.setOnKeyListener(new BackKeyListener(this));

        // progress bar view
        progressBar = view.findViewById(R.id.azimuth_slope_progress);
        progressBar.setMax(progressMaxValue);

        initCameraPreview(view);

        TextView azimuthView = view.findViewById(R.id.azimuth_slope_azimuth_value);
        TextView azimuthAccuracyView = view.findViewById(R.id.azimuth_slope_azimuth_accuracy);
        TextView slopeView = view.findViewById(R.id.azimuth_slope_slope_value);
        TextView slopeAccuracyView = view.findViewById(R.id.azimuth_slope_slope_accuracy);

        // create the Dialog
        AlertDialog alertDialg = builder.create();

        startAzimuthProcessor(azimuthView, azimuthAccuracyView);
        startSlopeProcessor(slopeView, slopeAccuracyView);

        return alertDialg;
    }

}
