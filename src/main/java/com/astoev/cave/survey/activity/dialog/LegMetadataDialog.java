package com.astoev.cave.survey.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.util.StringUtils;

public class LegMetadataDialog extends DialogFragment  {

    private String mTitle;
    private Leg mLeg;


    public LegMetadataDialog() {

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);
        builder.setIcon(R.drawable.ic_info_white);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.leg_metadata_dialog, null);
        builder.setView(view);

        TextView timestamp = view.findViewById(R.id.meta_date);
        timestamp.setText(StringUtils.dateToDateTimeString(mLeg.getDate()));

        return builder.create();
    }


    public void setTitle(String aTitle) {
        mTitle = aTitle;
    }

    public void setLeg(Leg aLeg) {
        mLeg = aLeg;
    }
}
