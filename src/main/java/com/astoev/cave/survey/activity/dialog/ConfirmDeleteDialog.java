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
 * Dialog for delete confirmation of model element.
 *
 * @author Zhivko Mitrev
 */
public class ConfirmDeleteDialog extends DialogFragment {

    public static final String DELETE_VECTOR_DIALOG = "DELETE_VECTOR_DIALOG";
    public static final String ELEMENT = "element";
    public static final String MESSAGE = "message";

    private DeleteHandler deleteHandler;

    /**
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceStateArg) {

        Bundle bundle = getArguments();
        final Serializable element = bundle != null ? bundle.getSerializable(ELEMENT) : null;
        final String message = bundle != null ? bundle.getString(MESSAGE) : null;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        builder.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // delete vector
                deleteHandler.delete(element);
            }
        });

        builder.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogArg, int whichArg) {
                // cancel
                ConfirmDeleteDialog.this.getDialog().cancel();
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            deleteHandler = (DeleteHandler)activity;
        } catch (ClassCastException cce) {
            Log.e(Constants.LOG_TAG_UI, "Failed to delete vector - Activity not an instance of DeleteVectorHandler", cce);
            UIUtilities.showNotification(R.string.error);
            throw cce;
        }
    }
}
