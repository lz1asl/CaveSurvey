package com.astoev.cave.survey.activity.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.main.MainActivity;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.service.ormlite.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends MainMenuActivity {

    private static boolean isFirstEntry;


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

        try {
            final List<Project> projects = mWorkspace.getDBHelper().getProjectDao().queryForAll();

            if (projects.size() > 0) {
                offerProjects();
            } else {
                Intent intent = new Intent(this, NewProjectActivity.class);
                startActivity(intent);
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

    public void loadProjectOnClick(View view) {

        Log.i(Constants.LOG_TAG_UI, "Load project");
        offerProjects();

    }

    private void offerProjects() {
        // prepare popup with projects available
        try {
            final List<Project> projects = mWorkspace.getDBHelper().getProjectDao().queryForAll();

            if (projects.size() == 0) {
                UIUtilities.showNotification(this, R.string.load_no_projects);
                return;
            }

            List<String> itemsList = new ArrayList<String>();
            for (Project p : projects) {
                itemsList.add(p.getName());
            }
            final CharSequence[] items = itemsList.toArray(new CharSequence[itemsList.size()]);

            Log.d(Constants.LOG_TAG_UI, "Display " + items.length + " projects");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.load_title);

            builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {

                    Log.i(Constants.LOG_TAG_UI, "Selected project " + projects.get(item));
                    mWorkspace.setActiveProject(projects.get(item));

                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to choose project", e);
            UIUtilities.showNotification(this, R.string.error);
        }
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
