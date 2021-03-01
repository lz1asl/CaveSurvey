package com.astoev.cave.survey.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.fragment.app.DialogFragment;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.util.ConfigUtil;

/**
 * Dialog for choosing vectors reading mode
 */
public class VectorsModeDialog extends DialogFragment {
    
    public static final int MODE_SINGLE = 0;
    public static final int MODE_SAVE_ON_RECEIVE = 1;
    public static final int MODE_SCAN = 2;

    /**
     * @see DialogFragment#onCreateDialog(Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceStateArg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.settings_vectors_mode);
        ArrayAdapter<String> modesAdaptor =
                new ArrayAdapter<>(this.getActivity(),
                        android.R.layout.simple_list_item_1,
                        getResources().getStringArray(R.array.settings_vectors_modes));

        builder.setAdapter(modesAdaptor, (dialogArg, mode) -> {
            Log.i(Constants.LOG_TAG_SERVICE, "Update vectors mode to " + mode);
            ConfigUtil.setIntProperty(ConfigUtil.PREF_VECTORS_MODE, mode);
            dismiss();
        });

        builder.setNegativeButton(android.R.string.cancel, (dialogArg, whichArg) -> {
            // cancel
            VectorsModeDialog.this.getDialog().cancel();
        });

        return builder.create();
    }

}
