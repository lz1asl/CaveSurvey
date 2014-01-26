package com.astoev.cave.survey.service;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.service.ormlite.DatabaseHelper;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.DaoUtil;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/25/12
 * Time: 3:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class Workspace {

    private static Workspace instance = null;

    private DatabaseHelper mDBHelper = null;

    private Workspace() {    }

    public static Workspace getCurrentInstance() {
        if (instance == null) {
            Log.v(Constants.LOG_TAG_SERVICE, "Initialize workspace");
            instance = new Workspace();
        }

        return instance;
    }

    public Integer getActiveProjectId() {
        int currProjectId = ConfigUtil.getIntProperty(ConfigUtil.PROP_CURR_PROJECT);
        if (currProjectId <=0 ) {
            return null;
        }
        return currProjectId;
    }

    public Project getActiveProject() {
        Integer currProjectId = getActiveProjectId();
        if (currProjectId == null ) {
            return null;
        }

        try {
            return DaoUtil.getProject(currProjectId);
        } catch (SQLException e) {
            return null;
        }
    }

    public void setActiveProject(Project aProject) {
        ConfigUtil.setIntProperty(ConfigUtil.PROP_CURR_PROJECT, aProject.getId());
    }

    public void setActiveLeg(Leg aLeg) {
        setActiveLegId(aLeg.getId());
        setActiveGalleryId(aLeg.getGalleryId());
    }

    private void setActiveLegId(Integer aId) {
        ConfigUtil.setIntProperty(ConfigUtil.PROP_CURR_LEG, aId);
    }

    public void clearActiveLeg() {
        ConfigUtil.removeProperty(ConfigUtil.PROP_CURR_LEG);
    }

//    private void setActiveGallery(Gallery aGallery) {
//        ConfigUtil.setIntProperty(ConfigUtil.PROP_CURR_GALLERY, aGallery.getId());
//    }

    private void setActiveGalleryId(Integer aGalleryId) {
        ConfigUtil.setIntProperty(ConfigUtil.PROP_CURR_GALLERY, aGalleryId);
    }

    public Gallery getActiveGallery() {
        Integer id = getActiveGalleryId();

        if (id == null) {
            return null;
        }

        try {
            return DaoUtil.getGallery(id);
        } catch (SQLException e) {
            Log.e(Constants.LOG_TAG_SERVICE, "Failet to get active gallery", e);
            return null;
        }
    }

    public Integer getActiveGalleryId() {
        Integer id = ConfigUtil.getIntProperty(ConfigUtil.PROP_CURR_GALLERY);

        if (id <= 0) {
            return null;
        }

        return id;
    }

    public void clearActiveGallery() {
        ConfigUtil.removeProperty(ConfigUtil.PROP_CURR_GALLERY);
    }

    public Integer getActiveLegId() {
        return ConfigUtil.getIntProperty(ConfigUtil.PROP_CURR_LEG);
    }

    public Leg getActiveOrFirstLeg() throws SQLException {
        Integer currLeg = getActiveLegId();
        if (currLeg != null) {
            return DaoUtil.getLeg(currLeg);
        } else {
            Integer currProject = getActiveProjectId();
            Log.i(Constants.LOG_TAG_SERVICE, "Search first leg for project " + currProject);
            QueryBuilder<Leg, Integer> firstLegQuery = mDBHelper.getLegDao().queryBuilder();
            firstLegQuery.where().eq(Leg.COLUMN_PROJECT_ID, currProject);
            firstLegQuery.orderBy(Leg.COLUMN_FROM_POINT, true);
            return mDBHelper.getLegDao().queryForFirst(firstLegQuery.prepare());
        }
    }

    /**
     * Helper method that queries for the active leg
     *
     * @return active Leg instance or null
     * @throws SQLException
     */
    public Leg getActiveLeg() throws SQLException {
        Integer currLeg = getActiveLegId();
        if (currLeg != null) {
            return DaoUtil.getLeg(currLeg);
        }
        return null;
    }

    public Leg getLastLeg(Integer aGalleryId) {
        try {
            int currProjectId = getActiveProjectId();
            Log.i(Constants.LOG_TAG_SERVICE, "Search last leg for project " + currProjectId);
            QueryBuilder<Leg, Integer> firstLegQuery = mDBHelper.getLegDao().queryBuilder();
            firstLegQuery.where().eq(Leg.COLUMN_PROJECT_ID, currProjectId).and().eq(Leg.COLUMN_GALLERY_ID, aGalleryId);
            firstLegQuery.orderBy(Leg.COLUMN_ID, false);
            return mDBHelper.getLegDao().queryForFirst(firstLegQuery.prepare());
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_SERVICE, "Failet to get last leg", e);
            return null;
        }
    }

    public Leg getLastLeg() {
        try {
            int currProjectId = getActiveProjectId();
            Log.i(Constants.LOG_TAG_SERVICE, "Search last leg for project " + currProjectId);
            QueryBuilder<Leg, Integer> firstLegQuery = mDBHelper.getLegDao().queryBuilder();
            firstLegQuery.where().eq(Leg.COLUMN_PROJECT_ID, currProjectId);
            firstLegQuery.orderBy(Leg.COLUMN_ID, false);
            return mDBHelper.getLegDao().queryForFirst(firstLegQuery.prepare());
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_SERVICE, "Failet to get last leg", e);
            return null;
        }
    }

    // TODO list need to be sorted, here last id is get, higher number is what we need
    public Point getLastGalleryPoint(Integer aGalleryId) throws SQLException {
        String lastPointInCurrentGalleryQuery = "select max(id) from points where id in(" +
                "select from_point_id from legs where gallery_id = " + aGalleryId +
                " union select to_point_id from legs where gallery_id = " + aGalleryId + ")";
        GenericRawResults<String[]> lastPointResults = mDBHelper.getPointDao().queryRaw(lastPointInCurrentGalleryQuery);
        try {
            String[] lastPointIdString = lastPointResults.getResults().get(0);
            return DaoUtil.getPoint(Integer.parseInt(lastPointIdString[0]));
        } finally {
            lastPointResults.close();
        }
    }

    public void clean() {
        instance = null;
    }

    public DatabaseHelper getDBHelper() {
        if (mDBHelper == null) {
            mDBHelper = new DatabaseHelper(ConfigUtil.getContext());
        }
        return mDBHelper;
    }

}
