package com.astoev.cave.survey.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.ConfigUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

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

        overrideDefaultExceptionHandler();

        updateLocale();
    }

    private void overrideDefaultExceptionHandler() {

        // store current handler
        final Thread.UncaughtExceptionHandler initialExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        // override
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {

                // log
                Log.e(Constants.LOG_TAG_SERVICE, "General exception occurred");
                if (paramThread != null) {
                    Log.e(Constants.LOG_TAG_SERVICE, "In Thread: " + paramThread.getName());
                }
                Log.e(Constants.LOG_TAG_SERVICE, "Cause: " + paramThrowable.getCause());

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw, true);
                paramThrowable.printStackTrace(pw);
                Log.e(Constants.LOG_TAG_SERVICE, "Trace: " + sw.getBuffer().toString());

                if (initialExceptionHandler != null) {
                    // if possible notify the default handler
                    Log.e(Constants.LOG_TAG_SERVICE, "Escalate");
                    initialExceptionHandler.uncaughtException(paramThread, paramThrowable);
                } else {
                    Log.e(Constants.LOG_TAG_SERVICE, "Can't retrow");
                    System.exit(1);
                }
            }
        });
    }

    /**
     * Updates the locale for every activity.
     */
    private void updateLocale() {
        if (ConfigUtil.getContext() == null) {
            ConfigUtil.setContext(this);
        }
        String languageToLoad = ConfigUtil.getStringProperty(ConfigUtil.PREF_LOCALE);
        if (languageToLoad != null) {

            String defaultLang = Locale.getDefault().getLanguage();
            if (!languageToLoad.equals(defaultLang)) {
                Locale locale = new Locale(languageToLoad);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
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
        if (getScreenTitle() != null) {
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
    protected String getScreenTitle() {
        return null;
    }

}
