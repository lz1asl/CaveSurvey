package com.astoev.cave.survey.activity.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.SettingsActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.dialog.ConfirmDeleteDialog;
import com.astoev.cave.survey.activity.dialog.DeleteHandler;
import com.astoev.cave.survey.activity.main.BTActivity;
import com.astoev.cave.survey.activity.main.MainActivity;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.util.DaoUtil;

import java.io.Serializable;
import java.util.List;

/**
 * Home activity for managing projects and general settings.
 *
 * @author Aleksander Stoev
 * @author Jivko Mitrev
 */
public class HomeActivity extends MainMenuActivity implements DeleteHandler {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
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
            case R.id.main_action_help:
                openHelp();
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

    private void openHelp() {
        Uri uri = Uri.parse("https://github.com/lz1asl/CaveSurvey/wiki/User-Guide");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void loadProjects() {
        try {

            ListView projectsContainer = (ListView) findViewById(R.id.homeProjects);

            final List<Project> projectsList = getWorkspace().getDBHelper().getProjectDao().queryForAll();

            if (projectsList.size() > 0) {
                Project[] projectsArray = new Project[projectsList.size()];
                projectsArray = projectsList.toArray(projectsArray);

                // populate the projects in the list using adapter
                ArrayAdapter<Project> projectsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, projectsArray);
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

                        final Project p = (Project) parent.getAdapter().getItem(position);
                        // instantiate dialog for confirming the delete and pass the selected project's id
                        String message = getString(R.string.home_delete_project, p.getName());
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(ConfirmDeleteDialog.ELEMENT, p.getId());
                        bundle.putString(ConfirmDeleteDialog.MESSAGE, message);

                        ConfirmDeleteDialog deleteVecotrDialog = new ConfirmDeleteDialog();
                        deleteVecotrDialog.setArguments(bundle);
                        deleteVecotrDialog.show(getSupportFragmentManager(), ConfirmDeleteDialog.DELETE_VECTOR_DIALOG);
                        return true;
                    }
                });

            } else {
                // no projects - show "No projects" label
                String[] value = {getResources().getString(R.string.home_no_projects)};
                ArrayAdapter<String> noprojectsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, value);
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

    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }

    /**
     * Receives the id of the project to be deleted from the confirmation dialog. Tries to delete
     * the selected project
     *
     * @param projectIdArg - id of the project confirmed for deleting
     */
    @Override
    public void delete(Serializable projectIdArg) {
        Log.i(Constants.LOG_TAG_UI, "Delete project");
        try {
            if (projectIdArg != null && projectIdArg instanceof Integer) {
                DaoUtil.deleteProject((Integer)projectIdArg);
                UIUtilities.showNotification(R.string.action_deleted);
                loadProjects();
            } else {
                String projectIdClass = projectIdArg != null ? projectIdArg.getClass().getName() : null;
                Log.e(Constants.LOG_TAG_UI, "Failed to delete project. Expected project it but:" + projectIdClass);
                UIUtilities.showNotification(R.string.error);
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to delete project", e);
            UIUtilities.showNotification(R.string.error);
        }
    }
}
