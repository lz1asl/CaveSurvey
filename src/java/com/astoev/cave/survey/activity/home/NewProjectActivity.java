package com.astoev.cave.survey.activity.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.BaseActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.main.PointActivity;
import com.astoev.cave.survey.model.*;
import com.astoev.cave.survey.util.PointUtil;
import com.j256.ormlite.misc.TransactionManager;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/23/12
 * Time: 4:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewProjectActivity extends BaseActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newproject);

        // populate spinners

        // distance
        prepareSpinner(R.id.options_units_distance, R.array.distance_units);
        prepareSpinner(R.id.options_distance_type, R.array.distance_read_type);

        // azimuth
        prepareSpinner(R.id.options_units_azimuth, R.array.azimuth_units);
        prepareSpinner(R.id.options_azimuth_type, R.array.azimuth_read_type);

        // slope
        prepareSpinner(R.id.options_units_slope, R.array.slope_units);
        prepareSpinner(R.id.options_slope_type, R.array.slope_read_type);

    }

    private void prepareSpinner(int aSpinnerId, int aTextArrayId) {
        Spinner distanceUnitsSpinner = (Spinner) findViewById(aSpinnerId);
        ArrayAdapter distanceUnitsAdapter = ArrayAdapter.createFromResource(this,aTextArrayId , android.R.layout.simple_spinner_item);
        distanceUnitsAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        distanceUnitsSpinner.setAdapter(distanceUnitsAdapter);
    }

    public void createNewProject(View view) {


        try {

            Log.v(Constants.LOG_TAG_UI, "Creating project");

            EditText projectNameField = (EditText) findViewById(R.id.new_projectname);
            final String newProjectName = projectNameField.getText().toString();

            //check existing
            List<Project> sameNameProjects = mWorkspace.getDBHelper().getProjectDao().queryForEq(Project.COLUMN_NAME, newProjectName);
            if (sameNameProjects.size() > 0) {
                UIUtilities.showNotification(this, R.string.home_button_new_exists, newProjectName);
                return;
            }

            Project project = TransactionManager.callInTransaction(mWorkspace.getDBHelper().getConnectionSource(),
                    new Callable<Project>() {
                        public Project call() throws SQLException {

                            // project
                            Project newProject = new Project();
                            newProject.setName(newProjectName);
                            newProject.setCreationDate(new Date());
                            mWorkspace.getDBHelper().getProjectDao().create(newProject);

                            // gallery
                            Gallery firstGallery = new Gallery();
                            firstGallery.setName(Gallery.getFirstGalleryName());
                            firstGallery.setProject(newProject);
                            Integer firstGalleryId = mWorkspace.getDBHelper().getGalleryDao().create(firstGallery);

                            // points
                            Point startPoint = PointUtil.createFirstPoint();
                            mWorkspace.getDBHelper().getPointDao().create(startPoint);
                            Point secondPoint = PointUtil.createSecondPoint();
                            mWorkspace.getDBHelper().getPointDao().create(secondPoint);

                            // first leg
                            Leg firstLeg = new Leg(startPoint, secondPoint, newProject, firstGalleryId);
                            mWorkspace.getDBHelper().getLegDao().create(firstLeg);

                            // default units








                            Option distanceOption = new Option(Option.CODE_DISTANCE_UNITS, Option.UNIT_METERS);
                            mWorkspace.getDBHelper().getOptionsDao().create(distanceOption);
                            Option azimuthOption = new Option(Option.CODE_AZIMUTH_UNITS, Option.UNIT_DEGREES);
                            mWorkspace.getDBHelper().getOptionsDao().create(azimuthOption);
                            Option slopeOption = new Option(Option.CODE_SLOPE_UNITS, Option.UNIT_DEGREES);
                            mWorkspace.getDBHelper().getOptionsDao().create(slopeOption);

                            Log.i(Constants.LOG_TAG_DB, "New project created");
                            return newProject;
                        }
                    });

            if (project != null) {
                mWorkspace.setActiveProject(project);

                Intent intent = new Intent(NewProjectActivity.this, PointActivity.class);
                mWorkspace.setActiveLegId(mWorkspace.getActiveOrFirstLeg().getId());
                intent.putExtra(Constants.LEG_SELECTED, mWorkspace.getActiveLegId());
                startActivity(intent);
            } else {
                Log.e(Constants.LOG_TAG_DB, "No project created");
                UIUtilities.showNotification(NewProjectActivity.this, R.string.error);
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed at new project creation", e);
            UIUtilities.showNotification(NewProjectActivity.this, R.string.error);
        }
    }
}