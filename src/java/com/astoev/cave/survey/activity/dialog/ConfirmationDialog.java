package com.astoev.cave.survey.activity.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;

import java.io.Serializable;

/**
 *
 * @author Zhivko Mitrev
 */
public class ConfirmationDialog extends DialogFragment {

    public static final String CONFIRM_DIALOG = "CONFIRM_DIALOG";
    public static final String OPERATION = "operation";
    public static final String MESSAGE = "message";
    public static final String TITLE = "title";

    private ConfirmationHandler confirmationHandler;

    /**
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceStateArg) {

        Bundle bundle = getArguments();
        final Serializable operationFromBundle = bundle != null ? bundle.getSerializable(OPERATION) : null;
        final String message = bundle != null ? bundle.getString(MESSAGE) : null;
        final String title = bundle != null ? bundle.getString(TITLE) : null;

        final ConfirmationOperation operation;
        if (operationFromBundle != null && operationFromBundle instanceof ConfirmationOperation){
            operation = (ConfirmationOperation)operationFromBundle;
        } else {
            operation = null;
            Log.e(Constants.LOG_TAG_UI, "Operation not supported:" + operationFromBundle);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // delete vector
                confirmationHandler.confirmOperation(operation);
            }
        });

        builder.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogArg, int whichArg) {
                // cancel
                ConfirmationDialog.this.getDialog().cancel();
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            confirmationHandler = (ConfirmationHandler)activity;
        } catch (ClassCastException cce) {
            Log.e(Constants.LOG_TAG_UI, "Activity not an instance of ConfirmationHandler", cce);
            UIUtilities.showNotification(R.string.error);
            throw cce;
        }
    }

}
