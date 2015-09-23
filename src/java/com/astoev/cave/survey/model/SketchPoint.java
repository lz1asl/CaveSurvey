package com.astoev.cave.survey.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Single point in a vector drawing.
 *
 */
@DatabaseTable(tableName = "sketch_element_points")
public class SketchPoint {

    public static final String COLUMN_ELEMENT_ID = "element_id";
    public static final String COLUMN_ORDER = "orderby";

    @DatabaseField(generatedId = true, columnName = "id")
    private Integer mId;
    @DatabaseField(canBeNull = false, columnName = COLUMN_ORDER)
    private Integer mOrderBy;
    @DatabaseField(canBeNull = false, columnName = "x")
    private float mX;
    @DatabaseField(canBeNull = false, columnName = "y")
    private float mY;
    @DatabaseField(canBeNull = false, foreign = true, columnName = COLUMN_ELEMENT_ID)
    private SketchElement mElement;


    public SketchPoint() {
    }

    public SketchPoint(SketchElement element) {
        mElement = element;
    }

    public SketchPoint(SketchElement element, float x, float y) {
        mElement = element;
        mX = x;
        mY = y;
    }

    public float getX() {
        return mX;
    }

    public void setX(float x) {
        mX = x;
    }

    public float getY() {
        return mY;
    }

    public void setY(float y) {
        mY = y;
    }

    public SketchElement getElement() {
        return mElement;
    }

    public void setElement(SketchElement element) {
        mElement = element;
    }

    public Integer getId() {
        return mId;
    }

    public void setId(Integer id) {
        mId = id;
    }

    public Integer getOrderBy() {
        return mOrderBy;
    }

    public void setOrderBy(Integer orderBy) {
        mOrderBy = orderBy;
    }
}
