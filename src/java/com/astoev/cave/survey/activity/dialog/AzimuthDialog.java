/**
 * 
 */
package com.astoev.cave.survey.activity.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.orientation.AzimuthChangedAdapter;
import com.astoev.cave.survey.service.orientation.AzimuthChangedListener;
import com.astoev.cave.survey.service.orientation.OrientationProcessor;
import com.astoev.cave.survey.service.orientation.OrientationProcessorFactory;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

/**
 * Azimuth dialog shows a progress bar that waits 3 seconds and reads an azimuth. It notifies the parent 
 * activity for the changed value if it implements AzimuthChangedListener.
 * 
 * @author jmitrev
 */
public class AzimuthDialog extends DialogFragment{

	 /** Max value for the progress bar*/
     private static int MAX_VALUE = 3;
     
     private TextView azimuthView;
     private TextView accuracyView;
     protected ProgressBar progressBar;
     
     /** Progress thread*/
	 protected ProgressThread progressThread;
	 
	 /** Progress handler*/
	 protected ProgressHandler progressHandler;
	 
	 /** OrientationProcessor that handles the work with the sensors*/
	 protected OrientationProcessor orientationProcessor;
	 
	 /** Formatter */
	 protected DecimalFormat formater;
	 
	 /** Flag if the azimuth is expected in degrees */
	 protected boolean isInDegrees = true;
	 
	 /** String for azimuth's units */
	 protected String unitsString;
	 
	 private float lastValue;
	 
	/**
	 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    super.onCreateDialog(savedInstanceState);
		
		formater = new DecimalFormat("#.#");
		
		if (Option.UNIT_DEGREES.equals(Options.getOptionValue(Option.CODE_AZIMUTH_UNITS))){
			isInDegrees = true;
			unitsString = " " + getString(R.string.degrees);
		} else {
			isInDegrees = false;
			unitsString = " " + getString(R.string.grads);
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.azimuth));
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.azimuth_dialog, null);
		builder.setView(view);

		// add key listener to handle the back button
		builder.setOnKeyListener(new BackKeyListener(this));
		
		// progress bar view 
		progressBar = (ProgressBar)view.findViewById(R.id.azimuth_progress);
		
		azimuthView = (TextView)view.findViewById(R.id.azimuth_value);
		accuracyView = (TextView)view.findViewById(R.id.azimuth_accuracy);
		
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
		        if (!isInDegrees){
		            lastValue = newValueArg * Constants.DEC_TO_GRAD;
		        } 
		        
		        azimuthView.setText(formater.format(lastValue) + unitsString);
		    }
		    
		    /**
		     * @see com.astoev.cave.survey.service.orientation.AzimuthChangedListener#onAccuracyChanged(int)
		     */
		    @Override
		    public void onAccuracyChanged(int accuracyArg) {
		        accuracyView.setText(orientationProcessor.getAccuracyAsString(accuracyArg));
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
	 * Helper method that handles when the dialog is handled. It will stop the thread and stop the azimuth 
	 * processor
	 */
	public void cancelDialog(){
		// stop the progress thread
		progressThread.setState(ProgressThread.STATE_DONE);
		
		// stop azimuth listener
		orientationProcessor.stopListening();
	}
	
	/**
	 * Helper method called to notify that progress bar is filled and the dialog should be dismissed. 
	 * Stops the azimuth processor notifies the parent activity and will dismiss the dialog
	 */
	protected void notifyEndProgress(){
		orientationProcessor.stopListening();
		Activity activity = getActivity();
		
		if (activity != null && activity instanceof AzimuthChangedListener){ 
			((AzimuthChangedListener)activity).onAzimuthChanged(lastValue);
		} 
		dismiss();
	}
	
	/**
     * Nested class that performs progress calculations (counting)
     */
    protected class ProgressThread extends Thread {
        Handler mHandler;
        final static int STATE_DONE = 0;
        final static int STATE_RUNNING = 1;
        int mState;
        int total;

        ProgressThread(Handler h) {
            mHandler = h;
        }

        public void run() {
            mState = STATE_RUNNING;
            total = 0;
            while (mState == STATE_RUNNING && total < MAX_VALUE + 1) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("ERROR", "Thread Interrupted");
                }
                Message msg = mHandler.obtainMessage();
                msg.arg1 = total;
                mHandler.sendMessage(msg);
                total++;
            }
        }

        /** 
         * Sets the current state for the thread, used to stop the thread 
         */
        public void setState(int state) {
            mState = state;
        }
    }// end of class ProgressThread
    
    /**
     * Define the Handler that receives messages from the thread and update the progress
     * 
     * @author astoev
     * @author jmitrev
     *
     */
    public static class ProgressHandler extends Handler{
    	
    	private WeakReference<AzimuthDialog> reference;
    	
    	public ProgressHandler(AzimuthDialog dialogFragmentArg)
    	{
    		reference = new WeakReference<AzimuthDialog>(dialogFragmentArg);
    	}
    	
        public void handleMessage(Message msg) {
        	
        	AzimuthDialog dialog = reference.get();
        	if (dialog == null){
        		return;
        	}
        	
            int total = msg.arg1;
            dialog.progressBar.setProgress(total);
            dialog.progressBar.setSecondaryProgress(total);
            if (total >= MAX_VALUE) {
            	dialog.progressThread.setState(ProgressThread.STATE_DONE);
            	dialog.notifyEndProgress();
            }
        }
    }
    
    public static class BackKeyListener implements OnKeyListener{
        
        private WeakReference<AzimuthDialog> reference;
        
        public BackKeyListener(AzimuthDialog dialogFragmentArg)
        {
            reference = new WeakReference<AzimuthDialog>(dialogFragmentArg);
        }
        
        @Override
        public boolean onKey(DialogInterface dialogArg, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                
                Log.i(Constants.LOG_TAG_UI, "Back button pressed! Cancel AzimuthDialog.");
                
                AzimuthDialog dialog= reference.get();
                if (dialog != null){
                    dialog.cancelDialog();
                }
            }
            return false;
        }
    }
}
