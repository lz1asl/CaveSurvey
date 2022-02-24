package com.astoev.cave.survey.activity.home;

import static com.astoev.cave.survey.service.export.excel.ExcelExport.EXCEL_FILE_EXTENSION;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.documentfile.provider.DocumentFile;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.main.PointActivity;
import com.astoev.cave.survey.activity.main.SurveyMainActivity;
import com.astoev.cave.survey.dto.ProjectConfig;
import com.astoev.cave.survey.fragment.InfoDialogFragment;
import com.astoev.cave.survey.fragment.ProjectFragment;
import com.astoev.cave.survey.manager.ProjectManager;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.service.imp.ExcelImport;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
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

    private static final String IMPORT_TOOLTIP_DIALOG = "IMPORT_TOOLTIP_DIALOG";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newproject);

        // load import section when needed
        Switch toggle = findViewById(R.id.import_toggle);
        View importContainer = findViewById(R.id.import_layout);
        importContainer.setVisibility(View.GONE);

        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                prepareImportFiles();
                importContainer.setVisibility(View.VISIBLE);
            } else {
                importContainer.setVisibility(View.GONE);
            }
        });
    }

    private void prepareImportFiles() {
        try {

            Spinner spinner = findViewById(R.id.import_files);

            // locate excel files
            List<DocumentFile> excelExportFiles = FileStorageUtil.listProjectFiles(null, EXCEL_FILE_EXTENSION);
            if (excelExportFiles == null || excelExportFiles.isEmpty()) {
                Log.i(Constants.LOG_TAG_UI, "No files to import");
            }

            List<NewProjectActivity.ImportFile> importFiles = new ArrayList<>();
            for (DocumentFile file : excelExportFiles) {
                importFiles.add(new ImportFile(file));
            }
            Collections.sort(importFiles);

            // default empty value
            importFiles.add(0, new ImportFile(null));

            ArrayAdapter<NewProjectActivity.ImportFile> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, importFiles);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to render import activity", e);
            UIUtilities.showNotification(R.string.error);
        }
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

            EditText projectNameField = findViewById(R.id.new_projectname);
            final String newProjectName = projectNameField.getText().toString();

            // name is required
            if (StringUtils.isEmpty(newProjectName)) {
                projectNameField.setError(getString(R.string.project_name_required));
                return;
            }

            // name should be simple enough to be presented on the FS
            DocumentFile projectHome = FileStorageUtil.getProjectHome(newProjectName);
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

            Spinner spinner = findViewById(R.id.import_files);
            boolean projectImported;
            if (spinner.getSelectedItem() == null
                    || ImportFile.NO_FILE_SELECTED.equals(spinner.getSelectedItem().toString())) {
                // no import options
                Log.v(Constants.LOG_TAG_UI, "No import selected");
                projectImported = false;
            } else {
                // import selected file
                Log.v(Constants.LOG_TAG_UI, "Importing project");

                Uri file = ((ImportFile) spinner.getSelectedItem()).uri;

                ExcelImport.importExcelFile(file, project);
                projectImported = true;
            }


            if (project != null) {
                getWorkspace().setActiveProject(project);
                getWorkspace().setActiveLeg(getWorkspace().getActiveOrFirstLeg());

                Intent intent;
                if (projectImported) {
                    intent = new Intent(NewProjectActivity.this, SurveyMainActivity.class);
                } else {
                    intent = new Intent(NewProjectActivity.this, PointActivity.class);
                    intent.putExtra(Constants.LEG_SELECTED, getWorkspace().getActiveLegId());
                }
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
			case R.id.new_action_create : {
				createNewProject();
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

    public void onChooseInfo(View viewArg) {
        InfoDialogFragment infoDialog = new InfoDialogFragment();

        Bundle bundle = new Bundle();
        String message = getString(R.string.settings_import_info);
        bundle.putString(InfoDialogFragment.MESSAGE, message);
        infoDialog.setArguments(bundle);

        infoDialog.show(getSupportFragmentManager(), IMPORT_TOOLTIP_DIALOG);
    }

    public static class ImportFile implements Comparable<ImportFile> {
        public static final String NO_FILE_SELECTED = " --- ";
        String name;
        Uri uri;

        public ImportFile(DocumentFile aFile) {

            if (aFile != null) {
                name = aFile.getName();
                uri = aFile.getUri();
            } else {
                name = NO_FILE_SELECTED;
                uri = null;
            }
        }

        @Override
        public String toString() {
            return name != null ? name : NO_FILE_SELECTED;
        }

        @Override
        public int compareTo(ImportFile another) {
            return name.compareTo(another.name);
        }
    }
    
}