package com.astoev.cave.survey.manager;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.dto.ProjectConfig;
import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.PointUtil;
import com.j256.ormlite.misc.TransactionManager;

import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * Manager to create edit and update Projects and project's configurations
 *
 * Created by Jivko on 15-3-9.
 *
 * @author Jivko Mitrev
 */
public class ProjectManager {

    /**
     * Instance method
     *
     * @return ProjectManager instance
     */
    public static ProjectManager instance(){
        return new ProjectManager();
    }

    /**
     * Create a Project by ProjectConfig
     *
     * @param projectConfig - holding the project configuration
     * @return Project created
     * @throws SQLException if there is a problem while creating the project
     */
    public Project createProject(final ProjectConfig projectConfig) throws SQLException{
        return TransactionManager.callInTransaction(getWorkspace().getDBHelper().getConnectionSource(),
                new Callable<Project>() {
                    public Project call() throws SQLException {

                        // project
                        Project newProject = new Project();
                        newProject.setName(projectConfig.getName());
                        newProject.setCreationDate(new Date());
                        getWorkspace().getDBHelper().getProjectDao().create(newProject);
                        getWorkspace().setActiveProject(newProject);

                        // gallery
                        Gallery firstGallery = DaoUtil.createGallery(true);

                        // points
                        Point startPoint = PointUtil.createFirstPoint();
                        getWorkspace().getDBHelper().getPointDao().create(startPoint);
                        Point secondPoint = PointUtil.createSecondPoint();
                        getWorkspace().getDBHelper().getPointDao().create(secondPoint);

                        // first leg
                        Leg firstLeg = new Leg(startPoint, secondPoint, newProject, firstGallery.getId());
                        getWorkspace().getDBHelper().getLegDao().create(firstLeg);
                        getWorkspace().setActiveLeg(firstLeg);

                        // project units
                        Log.i(Constants.LOG_TAG_UI, projectConfig.toString());
                        Options.createOption(Option.CODE_DISTANCE_UNITS, projectConfig.getDistanceUnits());
                        Options.createOption(Option.CODE_DISTANCE_SENSOR, projectConfig.getDistanceSensor());
                        Options.createOption(Option.CODE_AZIMUTH_UNITS, projectConfig.getAzimuthUnits());
                        Options.createOption(Option.CODE_AZIMUTH_SENSOR, projectConfig.getAzimuthSensor());
                        Options.createOption(Option.CODE_SLOPE_UNITS, projectConfig.getSlopeUnits());
                        Options.createOption(Option.CODE_SLOPE_SENSOR, projectConfig.getSlopeSensor());

                        return newProject;
                    }
                });
    }

    /**
     * Updates Project's configuration. Could update only the name and the sensor's options. Will
     * not update or take care of any units options as it requires full recalculation of the data
     * base.
     *
     * @param projectConfigArg - new Configuration
     * @throws SQLException if there is a problem during update
     */
    public void updateProject(final ProjectConfig projectConfigArg) throws SQLException{
        TransactionManager.callInTransaction(getWorkspace().getDBHelper().getConnectionSource(),
                new Callable<Project>() {
                    public Project call() throws SQLException {

                        Project project = Workspace.getCurrentInstance().getActiveProject();
                        String newName = projectConfigArg.getName();

                        // update project name if needed
                        if (!project.getName().equals(projectConfigArg.getName())){
                            project.setName(newName);
                            Workspace.getCurrentInstance().getDBHelper().getProjectDao().update(project);

                            Log.i(Constants.LOG_TAG_DB, "Project name updated to: " + newName);
                        }

                        Options.updateOption(Option.CODE_DISTANCE_SENSOR, projectConfigArg.getDistanceSensor());
                        Options.updateOption(Option.CODE_AZIMUTH_SENSOR, projectConfigArg.getAzimuthSensor());
                        Options.updateOption(Option.CODE_SLOPE_SENSOR, projectConfigArg.getSlopeSensor());
                        return project;
                    }
                });
    }

    /**
     * Helper method to acquire the Workspace
     *
     * @return Workspace instance
     */
    protected Workspace getWorkspace() {
        return Workspace.getCurrentInstance();
    }
}
