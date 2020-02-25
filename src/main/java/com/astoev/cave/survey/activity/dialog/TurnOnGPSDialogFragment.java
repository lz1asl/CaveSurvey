/**
 * 
 */
package com.astoev.cave.survey.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;

import com.astoev.cave.survey.R;

/**
 * DialogFragment that creates the dialog for asking the user if it would like to enable the GPS if it is 
 * enabled
 * 
 * @author jmitrev
 */
public class TurnOnGPSDialogFragment extends DialogFragment {

    /**
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceStateArg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.gps_enable_question);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialogArg, int whichArg) {
                // start activity to visit settings to enable gps
                startActivity( new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialogArg, int whichArg) {
                // cancel
                TurnOnGPSDialogFragment.this.getDialog().cancel();
            }
        });
        
        return builder.create();
    }
    
}
