package com.astoev.cave.survey.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.service.Workspace;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/29/12
 * Time: 5:43 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(Constants.LOG_TAG_UI, "Creating activity " + this.getClass().getName());

        super.onCreate(savedInstanceState);
    }

	/**
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		// set desired screen title as requested by the child implementation
        String screenTitle = getScreenTitle();
        if (getScreenTitle() != null){
        	setTitle(screenTitle);
        }
	}

    protected Workspace getWorkspace() {
        return Workspace.getCurrentInstance();
    }
    
    /**
     * Defines a default screen title to be shown as Activity's title
     * 
     * @return String title
     */
    protected String getScreenTitle(){
        return null;
    }

}
