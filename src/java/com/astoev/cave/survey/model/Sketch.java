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
@DatabaseTable(tableName = "sketches")
public class Sketch {

    public static final String COLUMN_POINT_ID = "point_id";
    public static final String COLUMN_GALLERY_ID = "gallery_id";

    @DatabaseField(generatedId = true, columnName = "id")
    private Integer mId;
    @DatabaseField(canBeNull = false, foreign = true, columnName = COLUMN_POINT_ID)
    private Point mPoint;
    @DatabaseField(canBeNull = false, columnName = COLUMN_GALLERY_ID)
    private Integer mGalleryId;
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

    public Integer getGalleryId() {
        return mGalleryId;
    }

    public void setGalleryId(Integer galleryId) {
        mGalleryId = galleryId;
    }
}
