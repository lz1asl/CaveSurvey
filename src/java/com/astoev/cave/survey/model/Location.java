package com.astoev.cave.survey.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/24/12
 * Time: 11:18 PM
 * To change this template use File | Settings | File Templates.
 */
@DatabaseTable(tableName = "locations")
public class Location {

    public static final String COLUMN_POINT_ID =               "point_id";

    @DatabaseField(generatedId = true, columnName = "id")
    private Integer mId;
    @DatabaseField(foreign = true, columnName =COLUMN_POINT_ID)
    private Point mPoint;
    @DatabaseField(columnName = "latitude")
    private double mLatitude;
    @DatabaseField(columnName = "longitude")
    private double mLongitude;
    @DatabaseField(columnName = "altitude")
    private int mAltitude;
    @DatabaseField(columnName = "accuracy")
    private int mAccuracy;

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

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double aLatitude) {
        mLatitude = aLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double aLongitude) {
        mLongitude = aLongitude;
    }

    public int getAltitude() {
        return mAltitude;
    }

    public void setAltitude(int aAltitude) {
        mAltitude = aAltitude;
    }

    public int getAccuracy() {
        return mAccuracy;
    }

    public void setAccuracy(int aAccuracy) {
        mAccuracy = aAccuracy;
    }
}
