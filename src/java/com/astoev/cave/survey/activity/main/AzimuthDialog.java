/**
 * 
 */
package com.astoev.cave.survey.activity.main;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.service.azimuth.AzimuthChangedListener;
import com.astoev.cave.survey.service.azimuth.AzimuthProcessor;
import com.astoev.cave.survey.service.azimuth.AzimuthProcessorFactory;

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

/**
 * Azimuth dialog shows a progress bar that waits 3 seconds and reads an azimuth. It notifies the parent 
 * activity for the changed value if it implements AzimuthChangedListener.
 * 
 * @author jmitrev
 */
public class AzimuthDialog extends DialogFragment implements AzimuthChangedListener{

	 /** Max value for the progress bar*/
     private static int MAX_VALUE = 3;
     
     private TextView azimuthView;
     private TextView accuracyView;
     private ProgressBar progressBar;
     
     /** Progress thread*/
	 private ProgressThread progressThread;
	 
	 /** Progress handler*/
	 private ProgressHandler progressHandler;
	 
	 /** AzimuthProcessor that handles the work with the sensors*/
	 private AzimuthProcessor azimuthProcessor;
	 
	 /** Formatter */
	 private DecimalFormat azimuthFrmater;
	 
	/**
	 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		azimuthFrmater = new DecimalFormat("#.#");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.azimuth));
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.azimuth_dialog, null);
		builder.setView(view);

		// add key listener to handle the back button
		builder.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					Log.i(Constants.LOG_TAG_UI, "Back button pressed! Cancel AzimuthDialog.");
					cancelDialog();
				}
				return false;
			}
		});
		
		// progress bar view 
		progressBar = (ProgressBar)view.findViewById(R.id.azimuth_progress);
		
		azimuthView = (TextView)view.findViewById(R.id.azimuth_value);
		accuracyView = (TextView)view.findViewById(R.id.azimuth_accuracy);
		
		// create the Dialog
		AlertDialog alertDialg = builder.create();
		
		// create azimuth processor to handle the azimuth sensors and value changes
		azimuthProcessor = AzimuthProcessorFactory.getAzimuthProcessor(getActivity(), this);
		azimuthProcessor.startListening();
		
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
	protected void cancelDialog(){
		// stop the progress thread
		progressThread.setState(ProgressThread.STATE_DONE);
		
		// stop azimuth listener
		azimuthProcessor.stopListening();
	}
	
	/**
	 * Helper method called to notify that progress bar is filled and the dialog should be dismissed. 
	 * Stops the azimuth processor notifies the parent activity and will dismiss the dialog
	 */
	protected void notifyEndProgress(){
		azimuthProcessor.stopListening();
		float lastValue = azimuthProcessor.getLastValue();
		int accuracy = azimuthProcessor.getAccuracy();
		Activity activity = getActivity();
		
		if (activity != null && activity instanceof AzimuthChangedListener){
			((AzimuthChangedListener)activity).onAzimuthChanged(lastValue, accuracy);
		} 
		dismiss();
	}
	
	/**
	 * Azimuth callback method. Edits the azimuth text view with the new value
	 * 
	 * @see com.astoev.cave.survey.service.azimuth.AzimuthChangedListener#onAzimuthChanged(float)
	 */
	@Override
	public void onAzimuthChanged(float newValueArg, int accuracyArg) {
		azimuthView.setText(azimuthFrmater.format(newValueArg));
		
		accuracyView.setText(azimuthProcessor.getAccuracyAsString(accuracyArg));
	}
	
    /**
     * Nested class that performs progress calculations (counting)
     */
    private class ProgressThread extends Thread {
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
}
