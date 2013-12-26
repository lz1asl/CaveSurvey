package com.astoev.cave.survey.model;

import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.PointUtil;
import com.astoev.cave.survey.util.StringUtils;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/24/12
 * Time: 11:15 PM
 * To change this template use File | Settings | File Templates.
 */
@DatabaseTable(tableName = "legs")
public class Leg implements Serializable {

    public static final String COLUMN_PROJECT_ID = "project_id";
    public static final String COLUMN_FROM_POINT = "from_point_id";
    public static final String COLUMN_TO_POINT = "to_point_id";
    public static final String COLUMN_DISTANCE_FROM_START = "distance_from_start";
    public static final String COLUMN_GALLERY_ID = "gallery_id";

    @DatabaseField(generatedId = true, columnName = "id")
    private Integer mId;
    @DatabaseField(canBeNull = false, foreign = true, columnName = COLUMN_FROM_POINT)
    private Point mFromPoint;
    @DatabaseField(foreign = true, canBeNull = false, columnName = COLUMN_TO_POINT)
    private Point mToPoint;
    @DatabaseField(foreign = true, canBeNull = false, foreignAutoRefresh = true, columnName = COLUMN_PROJECT_ID)
    private Project mProject;
    @DatabaseField(columnName = "distance")
    private Float mDistance;
    @DatabaseField(columnName = COLUMN_DISTANCE_FROM_START)
    private Float mDistanceFromStart;
    @DatabaseField(columnName = "azimuth")
    private Float mAzimuth;
    @DatabaseField(columnName = "slope")
    private Float mSlope;
    @DatabaseField(columnName = "left")
    private Float mLeft;
    @DatabaseField(columnName = "right")
    private Float mRight;
    @DatabaseField(columnName = "top")
    private Float mTop;
    @DatabaseField(columnName = "down")
    private Float mDown;
    @DatabaseField(canBeNull = false, columnName = COLUMN_GALLERY_ID)
    private Integer mGalleryId;


    public Leg() {

    }

    public Leg(Point fromPoint, Point toPoint, Project project, Integer aGalleryId) {
        this.mFromPoint = fromPoint;
        this.mToPoint = toPoint;
        this.mProject = project;
        mGalleryId = aGalleryId;
    }

    public Leg createNextLeg() throws SQLException {
        Point nextPoint = PointUtil.generateNextPoint(mGalleryId);
        Leg leg = new Leg(mToPoint, nextPoint, mProject, mGalleryId);

        return leg;
    }

    /**
     * Helper method to build string representation of the leg. Shows from and to points.
     * 
     * @return String representation
     * @throws SQLException
     */
    public String buildLegDescription() throws SQLException {
    	return buildLegDescription(false);
    }
    
    /**
     * Helper method to build string representation of the leg. Shows from and to points.
     * 
     * @param shortArg - flag if the representation should be short, without spaces.
     * @return String representation
     * @throws SQLException
     */
    public String buildLegDescription(boolean shortArg) throws SQLException{
        Workspace workspace = Workspace.getCurrentInstance();
        Point startPoint = (Point) workspace.getDBHelper().getPointDao().queryForId(getFromPoint().getId());
        Point endPoint = (Point) workspace.getDBHelper().getPointDao().queryForId(getToPoint().getId());
        
        StringBuilder builder = new StringBuilder(StringUtils.SPACE);
        builder.append(startPoint.getName());
        if (!shortArg){
        	builder.append(StringUtils.SPACE);
        }
        builder.append("->");
        if (!shortArg){
        	builder.append(StringUtils.SPACE);
        }
        builder.append(endPoint.getName());
        return builder.toString();
    }

    public static Note getActiveLegNote(Leg aActiveLeg, Workspace aWorkspace) throws SQLException {
        QueryBuilder<Note, Integer> query = aWorkspace.getDBHelper().getNoteDao().queryBuilder();
        query.where().eq(Note.COLUMN_POINT_ID, aActiveLeg.getFromPoint().getId());
        return (Note) aWorkspace.getDBHelper().getNoteDao().queryForFirst(query.prepare());
    }

    public Integer getId() {
        return mId;
    }

    public void setId(Integer aId) {
        mId = aId;
    }

    public Point getFromPoint() {
        return mFromPoint;
    }

    public void setFromPoint(Point aFromPoint) {
        mFromPoint = aFromPoint;
    }

    public Point getToPoint() {
        return mToPoint;
    }

    public void setToPoint(Point aToPoint) {
        mToPoint = aToPoint;
    }

    public Project getProject() {
        return mProject;
    }

    public void setProject(Project aProject) {
        mProject = aProject;
    }

    public Float getDistance() {
        return mDistance;
    }

    public void setDistance(Float aDistance) {
        mDistance = aDistance;
    }

    public Float getDistanceFromStart() {
        return mDistanceFromStart;
    }

    public void setDistanceFromStart(Float aDistanceFromStart) {
        mDistanceFromStart = aDistanceFromStart;
    }

    public Float getAzimuth() {
        return mAzimuth;
    }

    public void setAzimuth(Float aAzimuth) {
        mAzimuth = aAzimuth;
    }

    public Float getSlope() {
        return mSlope;
    }

    public void setSlope(Float aSlope) {
        mSlope = aSlope;
    }

    public Float getLeft() {
        return mLeft;
    }

    public void setLeft(Float aLeft) {
        mLeft = aLeft;
    }

    public Float getRight() {
        return mRight;
    }

    public void setRight(Float aRight) {
        mRight = aRight;
    }

    public Float getTop() {
        return mTop;
    }

    public void setTop(Float aTop) {
        mTop = aTop;
    }

    public Float getDown() {
        return mDown;
    }

    public void setDown(Float aDown) {
        mDown = aDown;
    }

    public Integer getGalleryId() {
        return mGalleryId;
    }

}
