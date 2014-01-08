package com.astoev.cave.survey.model;

import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.PointUtil;
import com.astoev.cave.survey.util.StringUtils;
import com.j256.ormlite.field.DatabaseField;
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

	private static final long serialVersionUID = 201312130309L;

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

    public boolean isNew(){
    	return (getId() ==  null);
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
        Point startPoint = null;
        Point endPoint = null;
        if (isNew()){
            startPoint = getFromPoint();
            endPoint = getToPoint();
        } else {
            startPoint = DaoUtil.getPoint(getFromPoint().getId());
            endPoint = DaoUtil.getPoint(getToPoint().getId());
        }
        
        StringBuilder builder = new StringBuilder(StringUtils.SPACE);
        Gallery legGallery = DaoUtil.getGallery(mGalleryId);
        Leg prevLeg = DaoUtil.getLegByToPoint(mFromPoint);
        Integer prevLegGalleryId = legGallery.getId();
        if (prevLeg != null) {
            prevLegGalleryId = prevLeg.getGalleryId();
        }
        if (mGalleryId.equals(prevLegGalleryId)) {
            builder.append(legGallery.getName());
        } else {
            builder.append(DaoUtil.getGallery(prevLegGalleryId).getName());
        }
        builder.append(startPoint.getName());
        if (!shortArg){
        	builder.append(StringUtils.SPACE);
        }
        builder.append("->");
        if (!shortArg){
        	builder.append(StringUtils.SPACE);
        }
        builder.append(legGallery.getName());
        builder.append(endPoint.getName());
        return builder.toString();
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

    public void setGalleryId(Integer aGalleryId) {
        mGalleryId = aGalleryId;
    }

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Leg [id=");
		builder.append(mId);
		builder.append(", fromPoint=");
		builder.append(mFromPoint);
		builder.append(", mToPoint=");
		builder.append(mToPoint);
		builder.append(", project=");
		builder.append(mProject);
//		builder.append(", mDistance=");
//		builder.append(mDistance);
//		builder.append(", mDistanceFromStart=");
//		builder.append(mDistanceFromStart);
//		builder.append(", mAzimuth=");
//		builder.append(mAzimuth);
//		builder.append(", mSlope=");
//		builder.append(mSlope);
//		builder.append(", mLeft=");
//		builder.append(mLeft);
//		builder.append(", mRight=");
//		builder.append(mRight);
//		builder.append(", mTop=");
//		builder.append(mTop);
//		builder.append(", mDown=");
//		builder.append(mDown);
		builder.append(", galleryId=");
		builder.append(mGalleryId);
		builder.append("]");
		return builder.toString();
	}

}
