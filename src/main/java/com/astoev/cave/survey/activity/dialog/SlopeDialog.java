/**
 * 
 */
package com.astoev.cave.survey.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.astoev.cave.survey.R;

/**
 * Slope dialog shows a progress bar that waits 3 seconds and reads an slope from internal sensor. It notifies 
 * the parent activity for the changed value if it implements SlopeChangedListener.
 * 
 * @author jmitrev
 */
public class SlopeDialog extends BaseBuildInMeasureDialog {
    
    /** Slope value view*/
    private TextView slopeView;
    private TextView slopeAccuracyView;
    

    /**
     * @see DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // parent initialization
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.slope));
        
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.slope_dialog, null);
        builder.setView(view);

        // add key listener to handle the back button
        builder.setOnKeyListener(new BackKeyListener(this));

        // progress bar view
        progressBar = (ProgressBar)view.findViewById(R.id.slope_progress);
        progressBar.setMax(progressMaxValue);

        slopeView = (TextView)view.findViewById(R.id.slope_value);
        slopeAccuracyView = (TextView) view.findViewById(R.id.slope_accuracy);
        
        // create the Dialog
        AlertDialog alertDialg = builder.create();
        
        startSlopeProcessor(slopeView, slopeAccuracyView);
        
        return alertDialg;
    }
    


}
