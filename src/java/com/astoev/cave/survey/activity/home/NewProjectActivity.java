package com.astoev.cave.survey.activity.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.main.ImportActivity;
import com.astoev.cave.survey.activity.main.PointActivity;
import com.astoev.cave.survey.dto.ProjectConfig;
import com.astoev.cave.survey.fragment.ProjectFragment;
import com.astoev.cave.survey.manager.ProjectManager;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.StringUtils;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/23/12
 * Time: 4:57 PM
 * To change this template use File | Settings | File Templates.
 *
 * @author Alexander Stoev
 * @author Zhivko Mitrev
 */
public class NewProjectActivity extends MainMenuActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newproject);
    }
    
    /**
	 * @see com.astoev.cave.survey.activity.BaseActivity#getScreenTitle()
	 */
	@Override
	protected String getScreenTitle() {
		return getString(R.string.new_title);
	}

    public void createNewProject() {
        try {

            Log.v(Constants.LOG_TAG_UI, "Creating project");

            ProjectFragment projectFragment = (ProjectFragment)getSupportFragmentManager().findFragmentById(R.id.project_container);
            final ProjectConfig projectConfig = projectFragment.getProjectConfig();

            EditText projectNameField = (EditText) findViewById(R.id.new_projectname);
            final String newProjectName = projectNameField.getText().toString();

            // name is required
            if (StringUtils.isEmpty(newProjectName)) {
                projectNameField.setError(getString(R.string.project_name_required));
                return;
            }

            // name should be simple enough to be presented on the FS
            File projectHome = FileStorageUtil.getProjectHome(newProjectName);
            if (projectHome == null) {
                projectNameField.setError(getString(R.string.project_name_characters));
                return;
            }

            //check existing
            List<Project> sameNameProjects = getWorkspace().getDBHelper().getProjectDao().queryForEq(Project.COLUMN_NAME, newProjectName);
            if (sameNameProjects.size() > 0) {
                projectNameField.setHint(R.string.home_button_new_exists);
                projectNameField.setError(getString(R.string.home_button_new_exists, newProjectName));
                return;
            }

            // create the project
            final Project project = ProjectManager.instance().createProject(projectConfig);

            if (project != null) {
                Intent intent = new Intent(NewProjectActivity.this, PointActivity.class);
                getWorkspace().setActiveProject(project);
                getWorkspace().setActiveLeg(getWorkspace().getActiveOrFirstLeg());
                intent.putExtra(Constants.LEG_SELECTED, getWorkspace().getActiveLegId());
                startActivity(intent);
                finish();
            } else {
                Log.e(Constants.LOG_TAG_DB, "No project created");
                UIUtilities.showNotification(R.string.error);
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed at new project creation", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

	/**
	 * @see com.astoev.cave.survey.activity.MainMenuActivity#getChildsOptionsMenu()
	 */
	@Override
	protected int getChildsOptionsMenu() {
		return R.menu.newprojectmenu;
	}

	/**
	 * Don't want to see base menu items
	 * 
	 * @see com.astoev.cave.survey.activity.MainMenuActivity#showBaseOptionsMenu()
	 */
	@Override
	protected boolean showBaseOptionsMenu() {
		return false;
	}

	/**
	 * @see com.astoev.cave.survey.activity.MainMenuActivity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(Constants.LOG_TAG_UI, "NewProject activity's menu selected - " + item.toString());
		
		switch (item.getItemId()) {
			case R.id.new_action_create :
				createNewProject();
				return true;

            case R.id.new_action_import :
                Intent intent = new Intent(NewProjectActivity.this, ImportActivity.class);
                startActivity(intent);
                return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}
    
}