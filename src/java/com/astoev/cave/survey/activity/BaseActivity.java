package com.astoev.cave.survey.activity;

import android.app.Activity;
import android.os.Bundle;
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
public abstract class BaseActivity extends Activity {

    protected Workspace mWorkspace = Workspace.getCurrentInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(Constants.LOG_TAG_UI, "Creating activity " + this.getClass().getName());
        super.onCreate(savedInstanceState);
    }

}
