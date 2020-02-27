package com.astoev.cave.survey.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.main.MainActivity;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.DaoUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog that selects another active leg. Currently sets as active the selected leg, stops the old
 * activity and sends and Intent for a new one
 *
 * @author Alexander Stoev
 * @author Zhivko Mitrev
 */
public class ChangeLegDialog extends DialogFragment{
    public static final String CHANGE_LEG = "CHANGE_LEG";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceStateArg) {
        try {
            final List<Leg> legs = DaoUtil.getCurrProjectLegs(false);
            List<String> itemsList = new ArrayList<>();
            int selectedItem = -1;
            int counter = 0;
            Integer activeLegId = Workspace.getCurrentInstance().getActiveLegId();
            for (Leg l : legs) {

                if (l.isMiddle()) {
                    continue;
                }

                itemsList.add(l.buildLegDescription());
                if (l.getId().equals(activeLegId)) {
                    selectedItem = counter;
                } else {
                    counter++;
                }
            }
            final CharSequence[] items = itemsList.toArray(new CharSequence[itemsList.size()]);

            Log.d(Constants.LOG_TAG_UI, "Display " + items.length + " legs");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.main_button_change_title);

            builder.setSingleChoiceItems(items, selectedItem, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {

                    Log.i(Constants.LOG_TAG_UI, "Selected leg " + legs.get(item));
                    Workspace.getCurrentInstance().setActiveLeg(legs.get(item));

                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    dialog.dismiss();
                    getActivity().finish();
                }
            });
            return builder.create();
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_DB, "Failed to select leg", e);
            UIUtilities.showNotification(R.string.error);
        }

        // fallback dialog in case of error
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_warning);
        builder.setMessage(R.string.error_list_legs);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });

        return builder.create();
    }
}
