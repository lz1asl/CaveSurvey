package com.astoev.cave.survey.service;

import android.util.Log;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.service.ormlite.DatabaseHelper;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/25/12
 * Time: 3:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class Workspace {

    private static Workspace instance = null;

    private Project mActiveProject;
    private Integer mActiveLegId;
    private DatabaseHelper mDBHelper;

    private Workspace() {

    }

    public static Workspace getCurrentInstance() {
        if (instance == null) {
            Log.v(Constants.LOG_TAG_SERVICE, "Initialize workspace");
            instance = new Workspace();
        }

        return instance;
    }

    public Leg getActiveOrFirstLeg() throws SQLException {
        if (mActiveLegId != null) {
            return (Leg) mDBHelper.getLegDao().queryForId(mActiveLegId);
        } else {
            Log.i(Constants.LOG_TAG_SERVICE, "Search first leg for project " + mActiveProject.getId());
            QueryBuilder<Leg, Integer> firstLegQuery = mDBHelper.getLegDao().queryBuilder();
            firstLegQuery.where().eq(Leg.COLUMN_PROJECT_ID, mActiveProject);
            firstLegQuery.orderBy(Leg.COLUMN_FROM_POINT, true);
            return (Leg) mDBHelper.getLegDao().queryForFirst(firstLegQuery.prepare());
        }
    }
    
    /**
     * Helper method that queries for the active leg
     * 
     * @return active Leg instance or null
     * @throws SQLException
     */
    public Leg getActiveLeg() throws SQLException {
        if (mActiveLegId != null) {
            return (Leg) mDBHelper.getLegDao().queryForId(mActiveLegId);
        }
        return null;
    }

    public Leg getLastLeg() throws SQLException {
        Log.i(Constants.LOG_TAG_SERVICE, "Search last leg for project " + mActiveProject.getId());
        QueryBuilder<Leg, Integer> firstLegQuery = mDBHelper.getLegDao().queryBuilder();
        firstLegQuery.where().eq(Leg.COLUMN_PROJECT_ID, mActiveProject);
        firstLegQuery.orderBy(Leg.COLUMN_FROM_POINT, false);
        return (Leg) mDBHelper.getLegDao().queryForFirst(firstLegQuery.prepare());
    }

    public List<Leg> getCurrProjectLegs() throws SQLException {
        QueryBuilder<Leg, Integer> statementBuilder = mDBHelper.getLegDao().queryBuilder();
        statementBuilder.where().eq(Leg.COLUMN_PROJECT_ID, mActiveProject);
        statementBuilder.orderBy(Leg.COLUMN_FROM_POINT, true);
        statementBuilder.orderBy(Leg.COLUMN_TO_POINT, true);
        statementBuilder.orderBy(Leg.COLUMN_DISTANCE_FROM_START, true);

        return mDBHelper.getLegDao().query(statementBuilder.prepare());
    }

    // TODO list need to be sorted, here last id is get, higher number is what we need
    public Point getLastGalleryPoint(Integer aGalleryId) throws SQLException {
        String lastPointInCurrentGalleryQuery = "select max(id) from points where id in(" +
                "select from_point_id from legs where gallery_id = " + aGalleryId +
                " union select to_point_id from legs where gallery_id = " + aGalleryId + ")";
        GenericRawResults<String[]> lastPointResults = mDBHelper.getPointDao().queryRaw(lastPointInCurrentGalleryQuery);
        try {
            String[] lastPointIdString = lastPointResults.getResults().get(0);
            return (Point) getDBHelper().getPointDao().queryForId(Integer.parseInt(lastPointIdString[0]));
        } finally {
            lastPointResults.close();
        }

    }

    public void clean() {
        mActiveLegId = null;
        mActiveProject = null;
        instance = null;
    }
    
    /**
     * Keeps the instance but leans the active leg and the active project
     */
    public void reset(){
        mActiveLegId = null;
        mActiveProject = null;
    }

    public Project getActiveProject() {
        return mActiveProject;
    }

    public void setActiveProject(Project aActiveProject) {
        mActiveProject = aActiveProject;
    }

    public Integer getActiveLegId() {
        return mActiveLegId;
    }

    public void setActiveLegId(Integer aActiveLegId) {
        mActiveLegId = aActiveLegId;
    }

    public DatabaseHelper getDBHelper() {
        return mDBHelper;
    }

    public void setDBHelper(DatabaseHelper aDBHelper) {
        mDBHelper = aDBHelper;
    }

}
