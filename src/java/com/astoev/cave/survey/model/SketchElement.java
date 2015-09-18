package com.astoev.cave.survey.model;

import com.astoev.cave.survey.activity.draw.DrawingOptions;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

/**
 * Created by astoev on 9/15/15.
 */
@DatabaseTable(tableName = "sketch_elements")
public class SketchElement {

    public static final String COLUMN_SKETCH_ID = "sketch_id";
    public static final String COLUMN_ORDER = "order";


    @DatabaseField(generatedId = true, columnName = "id")
    private Integer mId;
    @DatabaseField(canBeNull = false, foreign = true, columnName = COLUMN_SKETCH_ID)
    private Sketch mSketch;
    @DatabaseField(canBeNull = false, columnName = "order")
    private Integer mOrder;
    @DatabaseField(canBeNull = false, columnName = "size")
    private Integer mSize;
    @DatabaseField(canBeNull = false, columnName = "type")
    private DrawingOptions.TYPES mType;
    @DatabaseField(canBeNull = false, columnName = "color")
    private int mColor;
    @ForeignCollectionField(eager = true, orderColumnName = SketchPoint.COLUMN_ORDER)
    private List<SketchPoint> mPath;


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

    public Integer getOrder() {
        return mOrder;
    }

    public void setOrder(Integer order) {
        mOrder = order;
    }

    public List<SketchPoint> getPath() {
        return mPath;
    }

    public void setPath(List<SketchPoint> path) {
        mPath = path;
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
}
