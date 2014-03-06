package com.astoev.cave.survey.activity;

import java.util.Locale;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.ConfigUtil;

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
        updateLocale();
    }
    
    /**
     * Updates the locale for every activity.
     */
    private void updateLocale(){
        if (ConfigUtil.getContext() == null){
            ConfigUtil.setContext(this);
        }
        String languageToLoad = ConfigUtil.getStringProperty(ConfigUtil.PREF_LOCALE);
        if (languageToLoad != null){
            
            String defaultLang = Locale.getDefault().getLanguage();
            if (!languageToLoad.equals(defaultLang)){
                Locale locale = new Locale(languageToLoad); 
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
            }
        }
    }

	/**
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
        ConfigUtil.setContext(this);
		
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
