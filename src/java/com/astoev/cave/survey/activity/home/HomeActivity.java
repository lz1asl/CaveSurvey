package com.astoev.cave.survey.activity.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.main.MainActivity;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.service.ormlite.DatabaseHelper;

import java.util.List;

public class HomeActivity extends MainMenuActivity {

    private static boolean isFirstEntry;

    @Override
    protected void onResume() {
        super.onResume();
        loadProjects();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mWorkspace.getDBHelper() == null) {
            DatabaseHelper helper = new DatabaseHelper(this);
            mWorkspace.setDBHelper(helper);
        }

        setContentView(R.layout.home);

        loadProjects();
    }

    private void loadProjects() {
        try {

            ScrollView projectsContainer = (ScrollView) findViewById(R.id.homeProjects);
            projectsContainer.removeAllViews();
            final List<Project> projects = mWorkspace.getDBHelper().getProjectDao().queryForAll();

            if (projects.size() > 0) {
                TableLayout projectsTable = new TableLayout(this);
                projectsContainer.addView(projectsTable);
                for (final Project p : projects) {
                    TableRow projectRow = new TableRow(this);

                    TextView projectName = new TextView(this);
                    projectName.setText(p.getName());

                    projectRow.addView(projectName);
                    projectsTable.addView(projectRow);

                    Button openProjectButton = new Button(this);
                    openProjectButton.setText(R.string.home_button_open_project);
                    openProjectButton.setGravity(Gravity.RIGHT);

                    projectRow.addView(openProjectButton);
                    openProjectButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i(Constants.LOG_TAG_UI, "Selected project " + p.getId());
                            mWorkspace.setActiveProject(p);

                            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            } else {
                TextView noProjectsLabel = new TextView(this);
                noProjectsLabel.setText(R.string.home_no_projects);

                projectsContainer.addView(noProjectsLabel);
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed offer project", e);
            UIUtilities.showNotification(this, R.string.error);
        }
    }

    public void newProjectOnClick(View view) {
        Log.i(Constants.LOG_TAG_UI, "New project");
        Intent intent = new Intent(this, NewProjectActivity.class);
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
                        mWorkspace.clean();
                        HomeActivity.this.moveTaskToBack(true);
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
