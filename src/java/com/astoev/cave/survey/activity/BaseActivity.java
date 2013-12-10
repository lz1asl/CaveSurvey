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

    protected Workspace mWorkspace = Workspace.getCurrentInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	this.onCreate(savedInstanceState, true);
    }
    
    /**
     * onCreate method with flag to show or hide the action bar
     * 
     * @param savedInstanceState - saved instance
     * @param useActionBar	     - flag to show or hide the action bar
     */
    protected void onCreate(Bundle savedInstanceState, boolean useActionBar) {
        Log.i(Constants.LOG_TAG_UI, "Creating activity " + this.getClass().getName());

        super.onCreate(savedInstanceState);
        
        if (!useActionBar){
        	getSupportActionBar().hide();
        }

    }

}
