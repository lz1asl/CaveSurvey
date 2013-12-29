package com.astoev.cave.survey.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/24/12
 * Time: 11:18 PM
 * To change this template use File | Settings | File Templates.
 */
@DatabaseTable(tableName = "photos")
public class Photo {

    public static final String COLUMN_POINT_ID  = "point_id";

    @DatabaseField(generatedId = true, columnName = "id")
    private Integer mId;
    @DatabaseField(foreign = true, columnName = COLUMN_POINT_ID)
    private Point mPoint;
    @DatabaseField(columnName = "path")
    private String mFSPath;

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

    public String getFSPath() {
        return mFSPath;
    }

    public void setFSPath(String aFSPath) {
        mFSPath = aFSPath;
    }
}
