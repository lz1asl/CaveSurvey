package com.astoev.cave.survey.activity.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.dialog.LanguageDialog;
import com.astoev.cave.survey.activity.main.BTActivity;
import com.astoev.cave.survey.activity.main.MainActivity;
import com.astoev.cave.survey.activity.poc.SensorTestActivity;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.StringUtils;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

/**
 * Home activity for managing projects and general settings.
 *
 * @author astoev
 * @author jmitrev
 */
public class HomeActivity extends MainMenuActivity {

    /**
     * Dialog name to enable Language dialog
     */
    private static final String LANGUAGE_DIALOG = "LANGUAGE_DIALOG";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // to be enabled only for debug purposes
        // runDumpLogThread();
    }

    // dump android logs to a file, see http://stackoverflow.com/questions/6175002/write-android-logcat-data-to-a-file
    private void runDumpLogThread() {
        new Thread() {
            @Override
            public void run() {
                InputStream in = null;
                OutputStream out = null;
                try {

                    Process process = Runtime.getRuntime().exec("logcat -d");
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));
                    File logFile = new File(FileStorageUtil.getStorageHome(), "CaveSurvey.log");
                    out = new FileOutputStream(logFile);

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        IOUtils.write(line, out);
                        Thread.sleep(100);
                    }

                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG_SERVICE, "Failed to copy output", e);
                } finally {
                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(out);
                }
            }
        }.start();
    }

    @Override
    protected void onResume() {
        // first we reset the ws

        // then we call the parent that will set a title depending on the active project
        super.onResume();

        loadProjects();
    }

    /**
     * @see com.astoev.cave.survey.activity.MainMenuActivity#getChildsOptionsMenu()
     */
    @Override
    protected int getChildsOptionsMenu() {
        return R.menu.homemenu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(Constants.LOG_TAG_UI, "Home menu selected - " + item.toString());
        switch (item.getItemId()) {
            case R.id.action_new_project: {
                newProjectOnClick();
                return true;
            }
            case R.id.action_setup_bt: {
                pairBtDevice();
                return true;
            }
            case R.id.action_azimuth_test: {
                onAzimuthTest();
                return true;
            }
            case R.id.main_action_about:
                showAboutDialog();
                return true;
            case R.id.main_action_help:
                openHelp();
                return true;
            case R.id.action_language:
                onLanguage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openHelp() {
        Uri uri = Uri.parse("https://github.com/lz1asl/CaveSurvey/wiki/User-Guide");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void onLanguage() {
        LanguageDialog languageDialog = new LanguageDialog();
        languageDialog.show(getSupportFragmentManager(), LANGUAGE_DIALOG);
    }

    private void showAboutDialog() {
        try {
            Dialog dialog = new Dialog(this);
            String title = getString(R.string.about_title) + StringUtils.SPACE + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            dialog.setTitle(title);
            dialog.setContentView(R.layout.about);
            TextView url = (TextView) dialog.findViewById(R.id.aboutUrl);
            Linkify.addLinks(url, Linkify.WEB_URLS);
            TextView url2 = (TextView) dialog.findViewById(R.id.aboutUrl2);
            Linkify.addLinks(url2, Linkify.WEB_URLS);
            dialog.show();
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed toshow about", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    private void loadProjects() {
        try {

            ListView projectsContainer = (ListView) findViewById(R.id.homeProjects);

            final List<Project> projectsList = getWorkspace().getDBHelper().getProjectDao().queryForAll();

            if (projectsList.size() > 0) {
                Project[] projectsArray = new Project[projectsList.size()];
                projectsArray = projectsList.toArray(projectsArray);

                // populate the projects in the list using adapter
                ArrayAdapter<Project> projectsAdapter = new ArrayAdapter<Project>(this, android.R.layout.simple_list_item_1, projectsArray);
                projectsContainer.setAdapter(projectsAdapter);

                // item clicked
                projectsContainer.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Project project = (Project) parent.getAdapter().getItem(position);

                        Log.i(Constants.LOG_TAG_UI, "Selected project " + project.getId());
                        getWorkspace().setActiveProject(project);
                        Leg lastProjectLeg = getWorkspace().getLastLeg();
                        getWorkspace().setActiveLeg(lastProjectLeg);

                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });

                // item long pressed
                projectsContainer.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        try {
                            final Project p = (Project) parent.getAdapter().getItem(position);
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(HomeActivity.this);
                            dialogBuilder.setMessage(getString(R.string.home_delete_project, p.getName()))
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Log.i(Constants.LOG_TAG_UI, "Delete project");
                                            try {
                                                DaoUtil.deleteProject(p.getId());
                                                UIUtilities.showNotification(R.string.action_deleted);
                                                loadProjects();
                                            } catch (Exception e) {
                                                Log.e(Constants.LOG_TAG_UI, "Failed to delete project", e);
                                                UIUtilities.showNotification(R.string.error);
                                            }
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                        }
                                    });
                            AlertDialog alert = dialogBuilder.create();
                            alert.show();
                            return true;
                        } catch (Exception e) {
                            Log.e(Constants.LOG_TAG_UI, "Failed to delete project", e);
                            UIUtilities.showNotification(R.string.error);
                            return false;
                        }
                    }
                });

            } else {
                // no projects - show "No projects" label
                String[] value = {getResources().getString(R.string.home_no_projects)};
                ArrayAdapter<String> noprojectsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, value);
                projectsContainer.setAdapter(noprojectsAdapter);

                projectsContainer.setAdapter(noprojectsAdapter);

                // item clicked listener
                OnItemClickListener projectClickedListener = new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Intent intent = new Intent(HomeActivity.this, NewProjectActivity.class);
                        startActivity(intent);
                    }
                };

                projectsContainer.setOnItemClickListener(projectClickedListener);
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed offer project", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    /**
     * Action method that handles click on "Bluetooth device"  button
     */
    private void pairBtDevice() {
        Intent intent = new Intent(this, BTActivity.class);
        startActivity(intent);
    }

    /**
     * Action method that handles click on Add new project button
     */
    private void newProjectOnClick() {
        Log.i(Constants.LOG_TAG_UI, "New project");
        Intent intent = new Intent(this, NewProjectActivity.class);
        startActivity(intent);
    }

    private void onAzimuthTest() {
        Log.i(Constants.LOG_TAG_UI, "Azimuth Test");
        Intent intent = new Intent(this, SensorTestActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.menu_exit_confirmation_question)
                .setCancelable(false)
                .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(Constants.LOG_TAG_UI, "Exit app");
                        getWorkspace().clean();
                        HomeActivity.this.moveTaskToBack(true);
                        System.exit(0);
                    }
                })
                .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = dialogBuilder.create();
        alert.show();
    }
}
