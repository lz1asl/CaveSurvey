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
import com.astoev.cave.survey.service.orientation.AzimuthChangedAdapter;
import com.astoev.cave.survey.service.orientation.AzimuthChangedListener;
import com.astoev.cave.survey.service.orientation.OrientationProcessorFactory;

/**
 * Azimuth dialog shows a progress bar that waits 3 seconds and reads an azimuth. It notifies the parent
 * activity for the changed value if it implements AzimuthChangedListener.
 *
 * @author jmitrev
 */
public class AzimuthDialog extends BaseBuildInMeasureDialog {

    private TextView azimuthView;
    private TextView accuracyView;

    protected AzimuthChangedListener targetView;

    /**
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // parent initialization
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.azimuth));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.azimuth_dialog, null);
        builder.setView(view);

        // add key listener to handle the back button
        builder.setOnKeyListener(new BackKeyListener(this));

        // progress bar view
        progressBar = (ProgressBar) view.findViewById(R.id.azimuth_progress);

        azimuthView = (TextView) view.findViewById(R.id.azimuth_value);
        accuracyView = (TextView) view.findViewById(R.id.azimuth_accuracy);

        // create the Dialog
        AlertDialog alertDialg = builder.create();

        // create azimuth processor to handle the azimuth sensors and value changes
        orientationProcessor = OrientationProcessorFactory.getOrientationProcessor(getActivity(), new AzimuthChangedAdapter() {

            /**
             * Azimuth callback method. Edits the azimuth text view with the new value
             *
             * @see com.astoev.cave.survey.service.orientation.AzimuthChangedListener#onAzimuthChanged(float)
             */
            @Override
            public void onAzimuthChanged(float newValueArg) {
                //convert to Grads if necessary
                lastValue = newValueArg;
                if (!isInDegrees) {
                    lastValue = newValueArg * Constants.DEC_TO_GRAD;
                }

                azimuthView.setText(formater.format(lastValue) + unitsString);
            }

            /**
             * @see com.astoev.cave.survey.service.orientation.AzimuthChangedListener#onAzimuthChanged(float)
             */
            @Override
            public void onAccuracyChanged(int accuracyArg) {
                accuracyView.setText(orientationProcessor.getAccuracyAsString(accuracyArg));
            }
        });

        orientationProcessor.startListening();

        return alertDialg;
    }

}
