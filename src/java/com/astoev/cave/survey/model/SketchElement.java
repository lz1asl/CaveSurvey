package com.astoev.cave.survey.model;

import com.astoev.cave.survey.activity.draw.DrawingOptions;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by astoev on 9/15/15.
 */
@DatabaseTable(tableName = "sketch_elements")
public class SketchElement {

    public static final String COLUMN_SKETCH_ID = "sketch_id";
    public static final String COLUMN_ORDER = "orderby";
    public static final String FIELD_POINTS = "points";


    @DatabaseField(generatedId = true, columnName = "id")
    private Integer mId;
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = COLUMN_SKETCH_ID, index = true)
    private Sketch mSketch;
    @DatabaseField(canBeNull = false, columnName = COLUMN_ORDER, index = true)
    private Integer mOrderBy;
    @DatabaseField(canBeNull = false, columnName = "size")
    private Integer mSize;
    @DatabaseField(canBeNull = false, columnName = "type")
    private DrawingOptions.TYPES mType;
    @DatabaseField(canBeNull = false, columnName = "color")
    private int mColor;
    @ForeignCollectionField(eager = true, orderColumnName = SketchPoint.COLUMN_ORDER)
    private ForeignCollection<SketchPoint> points;

    public SketchElement() {
    }

    public SketchElement(Sketch sketch) {
        mSketch = sketch;
    }


    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
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

    public Integer getSize() {
        return mSize;
    }

    public void setSize(Integer size) {
        mSize = size;
    }

    public Sketch getSketch() {
        return mSketch;
    }

    public void setSketch(Sketch sketch) {
        mSketch = sketch;
    }

    public DrawingOptions.TYPES getType() {
        return mType;
    }

    public void setType(DrawingOptions.TYPES type) {
        mType = type;
    }

    public ForeignCollection<SketchPoint> getPoints() {
        return points;
    }

    public void setPoints(ForeignCollection<SketchPoint> points) {
        this.points = points;
    }
}
