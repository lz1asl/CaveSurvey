package com.astoev.cave.survey.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

/**
 * Fragment that Shows configurable message
 *
 * @author Zhivko Mitrev
 */
public class InfoDialogFragment extends DialogFragment {

    public static final String MESSAGE = "message";

    /**
     * @see DialogFragment#onCreateDialog(Bundle)
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceStateArg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle arguments = getArguments();
        if (arguments != null){
            String message  = arguments.getString(MESSAGE);
            builder.setMessage(message);
        }

        return builder.create();
    }



}
