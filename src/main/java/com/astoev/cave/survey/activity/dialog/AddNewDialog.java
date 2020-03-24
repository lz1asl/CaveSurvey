package com.astoev.cave.survey.activity.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;

/**
 * Creates dialog for adding next leg, gallery or middle point. It will notify back the activity for
 * the selected item that will be responsible for handling the choice. The activity should implement
 * AddNewSelectedHandler
 *
 * @author Jivko Mitrev
 */
public class AddNewDialog extends DialogFragment {

    /** Dialog name to enable choose sensors tooltip dialog */
    public static final String ADD_NEW_DIALOG = "ADD_NEW_DIALOG";

    private static final int[] ADD_ITEM_LABELS = {R.string.main_add_leg,
            R.string.main_add_gallery,
            R.string.main_add_middlepoint
    };

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String[] labels = new String[ADD_ITEM_LABELS.length];
        for (int i = 0; i < ADD_ITEM_LABELS.length; i++) {
            labels[i] = getString(ADD_ITEM_LABELS[i]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.main_add_title);

        ListAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, labels);

        builder.setSingleChoiceItems(adapter, -1, (dialog, item) -> {
            Activity activity = getActivity();
            if (activity instanceof AddNewSelectedHandler) {
                ((AddNewSelectedHandler) activity).addNewSelected(item);
            } else {
                Log.e(Constants.LOG_TAG_UI, "Parent activity not instance of AddNewSelectedHandler");
                UIUtilities.showNotification(R.string.error);
            }
        });

        return builder.create();
    }
}
