package com.astoev.cave.survey.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.util.UIUtilities;
import com.astoev.cave.survey.service.reports.ErrorReporter;
import com.astoev.cave.survey.util.StringUtils;

/**
 * Dialog that allows the user to enter some message before submitting the error report.
 *
 * Created by astoev on 11/6/15.
 */
public class ErrorReporterDialog extends DialogFragment {

    private boolean reportSent = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        final String dumpFile = getArguments().getString("dumpFile");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.error_reporter_title);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.error_reporter_dialog, null);
        builder.setView(view);


        Button sendButton = view.findViewById(R.id.erro_reporter_submit_button);
        sendButton.setOnClickListener(v -> {

            // get the message
            EditText messageBox = view.findViewById(R.id.error_reporter_message_box);

            if (StringUtils.isEmpty(messageBox)) {
                // validate user description is required
                messageBox.setError(getString(R.string.error_reporter_message_empty));
            } else {
                // report
                ErrorReporter.reportToServer(messageBox.getText().toString(), dumpFile);
                reportSent = true;
                dismiss();
            }

        });

        return  builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!reportSent) {
            UIUtilities.showNotification(R.string.error_not_sent);
        }
    }
}
