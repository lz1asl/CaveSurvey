package com.astoev.cave.survey.activity.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.home.SplashActivity;

/**
 * Request home folder permission warning dialog.
 */
public class StorageDialog extends DialogFragment  {

    public static final String STORAGE_DETAILS_URL = "https://github.com/lz1asl/CaveSurvey/wiki/User-Guide#installation";


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

        TextView storageDetails = view.findViewById(R.id.splashStorageDetails);
        storageDetails.setOnClickListener( l -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(STORAGE_DETAILS_URL))));

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
