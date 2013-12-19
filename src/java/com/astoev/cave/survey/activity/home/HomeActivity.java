package com.astoev.cave.survey.activity.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.main.BTActivity;
import com.astoev.cave.survey.activity.main.MainActivity;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.service.ormlite.DatabaseHelper;

import java.util.List;

/**
 * 
 * @author astoev
 * @author jmitrev
 */
public class HomeActivity extends MainMenuActivity {

    private static boolean isFirstEntry;
    
//    private void prepareBT() {
//        // BT protocol
//        Button btButton = (Button) findViewById(R.id.bt_setup_button);
//        if (!BluetoothService.isBluetoothSupported()) {
//            Log.w(Constants.LOG_TAG_UI, "Bluetooth not supported");
//            btButton.setEnabled(false);
//            btButton.setText(R.string.bt_not_supported);
//        } else {
//            btButton.setEnabled(true);
//        }
//    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        if (mWorkspace.getDBHelper() == null) {
            DatabaseHelper helper = new DatabaseHelper(this);
            mWorkspace.setDBHelper(helper);
        }
        
//        loadProjects();
    }
    
    @Override
    protected void onResume() {
    	// first we reset the ws
    	mWorkspace.reset();
    	
    	// then we call the parent that will set a title depending on the active project
        super.onResume();
    	
        loadProjects();
//        prepareBT();
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
		case R.id.action_new_project:{
			newProjectOnClick();
			return true;
		}
		case R.id.action_setup_bt : {
			pairBtDevice();
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void loadProjects() {
        try {

            ListView projectsContainer = (ListView) findViewById(R.id.homeProjects);
            
            final List<Project> projectsList = mWorkspace.getDBHelper().getProjectDao().queryForAll();
            
            if (projectsList.size() > 0) {
                Project[] projectsArray = new Project[projectsList.size()];
                projectsArray = projectsList.toArray(projectsArray);
            	
                // populate the projects in the list using adapter
            	ArrayAdapter<Project> projectsAdapter = new ArrayAdapter<Project>(this, android.R.layout.simple_list_item_1, projectsArray);
            	projectsContainer.setAdapter(projectsAdapter);
            	
            	// item clicked listener
            	OnItemClickListener projectClickedListener = new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

						Project project = (Project)parent.getAdapter().getItem(position);
						
                        Log.i(Constants.LOG_TAG_UI, "Selected project " + project.getId());
                        mWorkspace.setActiveProject(project);

                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        startActivity(intent);
					}
				};
				
            	projectsContainer.setOnItemClickListener(projectClickedListener);
            	
            } else {
            	// no projects - show "No projects" label
            	String[] value = {getResources().getString(R.string.home_no_projects)};
            	ArrayAdapter<String> noprojectsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, value);
            	projectsContainer.setAdapter(noprojectsAdapter);

                projectsContainer.setAdapter(noprojectsAdapter);
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed offer project", e);
            UIUtilities.showNotification(this, R.string.error);
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
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.menu_exit_confirmation_question)
                .setCancelable(false)
                .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(Constants.LOG_TAG_UI, "Exit app");
                        mWorkspace.clean();
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
