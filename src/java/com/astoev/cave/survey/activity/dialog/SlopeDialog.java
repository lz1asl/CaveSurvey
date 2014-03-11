/**
 * 
 */
package com.astoev.cave.survey.activity.dialog;

import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.orientation.OrientationProcessorFactory;
import com.astoev.cave.survey.service.orientation.SlopeChangedAdapter;
import com.astoev.cave.survey.service.orientation.SlopeChangedListener;

/**
 * Slope dialog shows a progress bar that waits 3 seconds and reads an slope from internal sensor. It notifies 
 * the parent activity for the changed value if it implements SlopeChangedListener.
 * 
 * @author jmitrev
 */
public class SlopeDialog extends AzimuthDialog {
    
    /** Slope value view*/
    private TextView slopeView;
    
    /** Last value for the slope from the sensor */
    private float lastValue;
    
    /**
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        formater = new DecimalFormat("#.#");
        
        if (Option.UNIT_DEGREES.equals(Options.getOptionValue(Option.CODE_SLOPE_UNITS))){
            isInDegrees = true;
            unitsString = " " + getString(R.string.degrees);
        } else {
            isInDegrees = false;
            unitsString = " " + getString(R.string.grads);
        }
        
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
        
        // create a handler and a thread that will drive the progress bar
        progressHandler = new ProgressHandler(this);
        progressThread = new ProgressThread(progressHandler);
        progressThread.start();
        
        return alertDialg;
    }
    
    /**
     * Helper method called to notify that progress bar is filled and the dialog should be dismissed. 
     * Stops the azimuth processor notifies the parent activity and will dismiss the dialog
     */
    protected void notifyEndProgress(){
        orientationProcessor.stopListening();
        Activity activity = getActivity();
        
        if (activity != null && activity instanceof SlopeChangedListener){ 
            ((SlopeChangedListener)activity).onSlopeChanged(lastValue);
        } 
        dismiss();
    }
}
