/**
 * 
 */
package com.astoev.cave.survey.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.service.orientation.OrientationProcessorFactory;
import com.astoev.cave.survey.service.orientation.SlopeChangedAdapter;

/**
 * Slope dialog shows a progress bar that waits 3 seconds and reads an slope from internal sensor. It notifies 
 * the parent activity for the changed value if it implements SlopeChangedListener.
 * 
 * @author jmitrev
 */
public class SlopeDialog extends BaseBuildInMeasureDialog {
    
    /** Slope value view*/
    private TextView slopeView;
    

    /**
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
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
        
        slopeView = (TextView)view.findViewById(R.id.slope_value);
        
        // create the Dialog
        AlertDialog alertDialg = builder.create();
        
        // create azimuth processor to handle the azimuth sensors and value changes
        orientationProcessor = OrientationProcessorFactory.getOrientationProcessor(getActivity(), new SlopeChangedAdapter() {

            /**
             * Slope callback method. Edits the slope text view with the new value
             * 
             * @see com.astoev.cave.survey.service.orientation.SlopeChangedAdapter#onSlopeChanged(float)
             */
            @Override
            public void onSlopeChanged(float newValueArg) {
                //convert to Grads if necessary
                lastValue = newValueArg;
                if (!isInDegrees){
                    lastValue = newValueArg * Constants.DEC_TO_GRAD;
                }
                
                slopeView.setText(formater.format(lastValue) + unitsString);
            }
        });

        orientationProcessor.startListening();
        
        return alertDialg;
    }
    


}
