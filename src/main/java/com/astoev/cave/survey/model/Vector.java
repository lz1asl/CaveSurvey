package com.astoev.cave.survey.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 5/6/12
 * Time: 10:20 PM
 * To change this template use File | Settings | File Templates.
 */
@DatabaseTable(tableName = "vectors")
public class Vector implements Serializable {

	private static final long serialVersionUID = 201401022247L;
	
    public static final String COLUMN_POINT = "point_id";
    public static final String COLUMN_GALLERY_ID = "gallery_id";
    public static final String COLUMN_ID = "id";

    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private Integer mId;
    @DatabaseField(canBeNull = false, foreign = true, columnName = COLUMN_POINT)
    private Point mPoint;
    @DatabaseField(canBeNull = false, columnName = COLUMN_GALLERY_ID)
    private Integer mGalleryId;
    @DatabaseField(columnName = "distance")
    private Float mDistance;
    @DatabaseField(columnName = "azimuth")
    private Float mAzimuth;
    @DatabaseField(columnName = "slope")
    private Float mSlope;

    public Vector() {
    }

    public Integer getId() {
        return mId;
    }

    public void setId(Integer aId) {
        mId = aId;
    }

    public Point getPoint() {
        return mPoint;
    }

    public void setPoint(Point aPoint) {
        mPoint = aPoint;
    }

    public Float getDistance() {
        return mDistance;
    }

    public void setDistance(Float aDistance) {
        mDistance = aDistance;
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

    public Integer getGalleryId() {
        return mGalleryId;
    }

    public void setGalleryId(Integer galleryId) {
        mGalleryId = galleryId;
    }
}
