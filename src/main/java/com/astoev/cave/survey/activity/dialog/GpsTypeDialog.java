package com.astoev.cave.survey.activity.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;

/**
 * Dialog that handles the selection of GPS. Is it going to be maual or automatic
 *
 * @author Jivko Mitrev
 */
public class GpsTypeDialog extends DialogFragment {

    public enum GPSType {AUTO, MANUAL}

    /** Dialog name to enable choose gps type dialog */
    public static final String GPS_TYPE_DIALOG = "GPS_TYPE_DIALOG";

    private static final int[] GPS_TYPE_LABELS =
            {R.string.gps_type_auto,
            R.string.gps_type_manual};

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String[] labels = new String[GPS_TYPE_LABELS.length];
        for (int i = 0; i < GPS_TYPE_LABELS.length; i++) {
            labels[i] = getString(GPS_TYPE_LABELS[i]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.gps_type_title);

        ListAdapter adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1, labels);
        builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int itemArg) {
                Activity activity = getActivity();
                if (activity instanceof GpsTypeHandler) {
                    GPSType gpsType = GPSType.AUTO;
                    if (1 == itemArg) {
                        gpsType = GPSType.MANUAL;
                    }
                    ((GpsTypeHandler) activity).gpsTypeSelected(gpsType);
                } else {
                    Log.e(Constants.LOG_TAG_UI, "Parent activity not instance of GpsTypeHandler");
                    UIUtilities.showNotification(R.string.error);
                }
            }
        });

        return builder.create();
    }
}

