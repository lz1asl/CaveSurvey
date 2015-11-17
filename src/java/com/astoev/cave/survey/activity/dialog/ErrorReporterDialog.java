package com.astoev.cave.survey.activity.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.service.reports.ErrorReporter;

/**
 * Dialog that allows the user to enter some message before submitting the error report.
 *
 * Created by astoev on 11/6/15.
 */
public class ErrorReporterDialog extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        final String dumpFile = getArguments().getString("dumpFile");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.error_reporter_title);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.error_reporter_dialog, null);
        builder.setView(view);


        Button sendButton = (Button) view.findViewById(R.id.erro_reporter_submit_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get the message
                EditText messageBox = (EditText) view.findViewById(R.id.error_reporter_message_box);

                // report
                ErrorReporter.reportToServer(messageBox.getText().toString(), dumpFile);
                dismiss();
            }
        });

        return  builder.create();
    }
}
