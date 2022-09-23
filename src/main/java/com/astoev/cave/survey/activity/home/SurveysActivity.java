package com.astoev.cave.survey.activity.home;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.config.SettingsActivity;
import com.astoev.cave.survey.activity.dialog.AboutDialog;
import com.astoev.cave.survey.activity.dialog.ConfirmDeleteDialog;
import com.astoev.cave.survey.activity.dialog.DeleteHandler;
import com.astoev.cave.survey.activity.main.BTActivity;
import com.astoev.cave.survey.activity.main.SurveyMainActivity;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.PermissionUtil;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Home activity for managing projects and general settings.
 *
 * @author Aleksander Stoev
 * @author Jivko Mitrev
 */
public class SurveysActivity extends MainMenuActivity implements DeleteHandler {

    private static final String ABOUT_DIALOG = "ABOUT_DIALOG";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.surveys);
    }

    @Override
    protected void onResume() {
        // first we reset the ws

        // then we call the parent that will set a title depending on the active project
        super.onResume();

        loadSurveysList();
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
        Log.i(Constants.LOG_TAG_UI, "Surveys menu selected - " + item.toString());
        switch (item.getItemId()) {
            case R.id.action_new_project: {
                newProjectOnClick();
                return true;
            }
            case R.id.action_setup_bt: {
                pairBtDevice();
                return true;
            }
            case R.id.main_action_about:
                openAbout();
                return true;
            case R.id.main_action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void openAbout() {
        AboutDialog aboutDialogFragment = new AboutDialog();
        aboutDialogFragment.show(getSupportFragmentManager(), ABOUT_DIALOG);
    }

    private void loadSurveysList() {
        try {

            ListView projectsContainer = findViewById(R.id.surveysList);

            final List<Project> projectsList = getWorkspace().getDBHelper().getProjectDao().queryForAll();
            Collections.sort(projectsList);

            if (projectsList.size() > 0) {
                Project[] projectsArray = new Project[projectsList.size()];
                projectsArray = projectsList.toArray(projectsArray);

                // populate the projects in the list using adapter
                ArrayAdapter<Project> projectsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, projectsArray);
                projectsContainer.setAdapter(projectsAdapter);

                // item clicked
                projectsContainer.setOnItemClickListener((parent, view, position, id) -> {

                    Object selectedObject = parent.getAdapter().getItem(position);
                    if (selectedObject != null && selectedObject instanceof Project) {
                        Project project = (Project) selectedObject;

                        Log.i(Constants.LOG_TAG_UI, "Selected survey " + project.getId());
                        getWorkspace().setActiveProject(project);
                        Leg lastProjectLeg = getWorkspace().getLastLeg();
                        getWorkspace().setActiveLeg(lastProjectLeg);

                        Intent intent = new Intent(SurveysActivity.this, SurveyMainActivity.class);
                        startActivity(intent);
                    }
                });

                // item long pressed
                projectsContainer.setOnItemLongClickListener((parent, view, position, id) -> {

                    Object selectedObject = parent.getAdapter().getItem(position);
                    if (selectedObject != null && selectedObject instanceof Project) {
                        Project project = (Project) selectedObject;
                        // instantiate dialog for confirming the delete and pass the selected project's id
                        String message = getString(R.string.home_delete_project, project.getName());
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(ConfirmDeleteDialog.ELEMENT, project.getId());
                        bundle.putString(ConfirmDeleteDialog.MESSAGE, message);

                        ConfirmDeleteDialog deleteVecotrDialog = new ConfirmDeleteDialog();
                        deleteVecotrDialog.setArguments(bundle);
                        deleteVecotrDialog.show(getSupportFragmentManager(), ConfirmDeleteDialog.DELETE_VECTOR_DIALOG);
                        return true;
                    }
                    return false;
                });

            } else {
                // no projects - show "No projects" label
                String[] value = {getResources().getString(R.string.home_no_projects)};
                ArrayAdapter<String> noprojectsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, value);
                projectsContainer.setAdapter(noprojectsAdapter);

                projectsContainer.setAdapter(noprojectsAdapter);

                // item clicked listener
                OnItemClickListener projectClickedListener = (parent, view, position, id) -> {

                    Intent intent = new Intent(SurveysActivity.this, NewProjectActivity.class);
                    startActivity(intent);
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

        if (!PermissionUtil.requestPermissions(new String[]{BLUETOOTH, BLUETOOTH_SCAN, BLUETOOTH_CONNECT, BLUETOOTH_ADMIN, ACCESS_FINE_LOCATION}, this, 301)) {
            return;
        }

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

    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }

    /**
     * Receives the id of the project to be deleted from the confirmation dialog. Tries to delete
     * the selected project
     *
     * @param surveyIdArg - id of the project confirmed for deleting
     */
    @Override
    public void delete(Serializable surveyIdArg) {
        Log.i(Constants.LOG_TAG_UI, "Delete project");
        try {
            if (surveyIdArg != null && surveyIdArg instanceof Integer) {
                DaoUtil.deleteProject((Integer)surveyIdArg);
                UIUtilities.showNotification(R.string.action_deleted);
                loadSurveysList();
            } else {
                String projectIdClass = surveyIdArg != null ? surveyIdArg.getClass().getName() : null;
                Log.e(Constants.LOG_TAG_UI, "Failed to delete survey. Expected project it but:" + projectIdClass);
                UIUtilities.showNotification(R.string.error);
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to delete survey", e);
            UIUtilities.showNotification(R.string.error);
        }
    }
}
