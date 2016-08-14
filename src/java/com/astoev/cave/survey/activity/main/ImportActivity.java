package com.astoev.cave.survey.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.fragment.InfoDialogFragment;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.service.export.excel.ExcelExport;
import com.astoev.cave.survey.service.imp.ExcelImport;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by astoev on 7/31/16.
 */
public class ImportActivity extends MainMenuActivity {

    private static final String IMPORT_TOOLTIP_DIALOG = "IMPORT_TOOLTIP_DIALOG";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_import);

        try {

            Spinner spinner = (Spinner) findViewById(R.id.import_files);

            // locate excel files
            List<File> excelExportFiles = FileStorageUtil.listProjectFiles(null, ExcelExport.EXCEL_FILE_EXTENSION);
            if (excelExportFiles == null || excelExportFiles.isEmpty()) {
                UIUtilities.showNotification(R.string.settings_import_no_files);
                Log.i(Constants.LOG_TAG_UI, "Mo files to import");
                return;
            }

            List<ImportFile> importFiles = new ArrayList<>();
            for (File file : excelExportFiles) {
                importFiles.add(new ImportFile(file));
            }
            Collections.sort(importFiles);

            ArrayAdapter<ImportFile> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, importFiles);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to render import activity", e);
            UIUtilities.showNotification(R.string.error);
        }

    }

    @Override
    protected String getScreenTitle() {
        return getString(R.string.settings_import_title);
    }

    @Override
    protected int getChildsOptionsMenu() {
        return R.menu.importmenu;
    }

    @Override
    protected boolean showBaseOptionsMenu() {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(Constants.LOG_TAG_UI, "Import activity's menu selected - " + item.toString());

        switch (item.getItemId()) {
            case R.id.import_create: {
                onImport();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onImport() {

        Log.v(Constants.LOG_TAG_UI, "Importing project");

        try {

            // validate
            EditText projectNameField = (EditText) findViewById(R.id.import_name);
            final String projectName = projectNameField.getText().toString();

            // name is required
            if (StringUtils.isEmpty(projectName)) {
                projectNameField.setError(getString(R.string.project_name_required));
                return;
            }

            // name should be simple enough to be presented on the FS
            File projectHome = FileStorageUtil.getProjectHome(projectName);
            if (projectHome == null) {
                projectNameField.setError(getString(R.string.project_name_characters));
                return;
            }

            //check existing
            List<Project> sameNameProjects = getWorkspace().getDBHelper().getProjectDao().queryForEq(Project.COLUMN_NAME, projectName);
            if (sameNameProjects.size() > 0) {
                projectNameField.setHint(R.string.home_button_new_exists);
                projectNameField.setError(getString(R.string.home_button_new_exists, projectName));
                return;
            }

            // no import options
            Spinner spinner = (Spinner) findViewById(R.id.import_files);
            if (spinner.getItemAtPosition(0) == null) {
                UIUtilities.showNotification(R.string.settings_import_no_files);
                return;
            }

            if (spinner.getSelectedItem() == null) {
                UIUtilities.showNotification(R.string.required);
                return;
            }

            // actual import

            File file = ((ImportFile) spinner.getSelectedItem()).file;

            Project p = ExcelImport.importExcelFile(file, projectName);
            if (p != null) {
                // open
                UIUtilities.showNotification(R.string.success);
                Log.i(Constants.LOG_TAG_UI, "Opening imported project " + p.getId());
                getWorkspace().setActiveProject(p);
                Leg lastProjectLeg = getWorkspace().getLastLeg();
                getWorkspace().setActiveLeg(lastProjectLeg);
                Intent intent = new Intent(ImportActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed at project import", e);
            UIUtilities.showNotification(R.string.error);
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

    class ImportFile implements Comparable {
        File file;

        public ImportFile(File aFile) {
            file = aFile;
        }

        @Override
        public String toString() {
            return file.getName();
        }

        @Override
        public int compareTo(Object another) {
            return file.getName().compareTo(((ImportFile)another).file.getName());
        }
    }
}
