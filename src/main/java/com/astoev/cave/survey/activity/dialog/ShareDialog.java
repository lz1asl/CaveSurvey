package com.astoev.cave.survey.activity.dialog;

import static com.astoev.cave.survey.Constants.LOG_TAG_SERVICE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.DialogFragment;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.service.export.zip.ZipExport;
import com.astoev.cave.survey.service.export.zip.ZipType;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.FileStorageUtil;

public class ShareDialog extends DialogFragment {



    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.share_title));
        builder.setIcon(R.drawable.ic_baseline_share_24);
        
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.share, null);
        builder.setView(view);

        // possible values
        Spinner shareMode = view.findViewById(R.id.share_type);
        ArrayAdapter adapterShareModes = ArrayAdapter.createFromResource(view.getContext(), R.array.share_type, android.R.layout.simple_spinner_item);
        adapterShareModes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shareMode.setAdapter(adapterShareModes);

        // action handler
        Button shareButton = view.findViewById(R.id.share_button);
        shareButton.setOnClickListener(l -> {
            ZipExport export = new ZipExport(this.getResources());
            export.setZipType(ZipType.fromIndex(shareMode.getSelectedItemPosition()));
            DocumentFile exportFile = null;
            try {
                exportFile = export.runExport(Workspace.getCurrentInstance().getActiveProject(), null, false);
                if (exportFile == null) {
                    UIUtilities.showNotification(ConfigUtil.getContext(), R.string.export_io_error, FileStorageUtil.getFullRelativePath(exportFile));
                } else {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, exportFile.getUri());
                    shareIntent.setType(exportFile.getType());
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent, null));
                    dismiss();
                }
            } catch (Exception aE) {
                Log.e(LOG_TAG_SERVICE, "Share failed", aE);
                UIUtilities.showNotification(ConfigUtil.getContext(), R.string.export_io_error, FileStorageUtil.getFullRelativePath(exportFile));
            }
        });

        // create the Dialog
        AlertDialog alertDialg = builder.create();
        return alertDialg;
    }



}
