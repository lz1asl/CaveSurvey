package com.astoev.cave.survey.activity.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.home.SplashActivity;

/**
 * Request home folder permission warning dialog.
 */
public class StorageDialog extends DialogFragment  {

    private SplashActivity parent;

    public StorageDialog(SplashActivity aSplashActivity) {
        parent = aSplashActivity;
    }

    /**
     * @see DialogFragment#onCreateDialog(Bundle)
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.storage_dialog, null);
        builder.setView(view);

        return  builder.create();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);

        // user cancelled, display error
        parent.displayFatalError(R.string.splash_error_storage);
    }

    public void onParentClose() {
        dismiss();
    }
}
