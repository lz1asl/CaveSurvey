package com.astoev.cave.survey.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/24/12
 * Time: 11:18 PM
 * To change this template use File | Settings | File Templates.
 */
@DatabaseTable(tableName = "sketches")
public class Sketch {

    @DatabaseField(generatedId = true, columnName = "id")
    private Integer mId;
    @DatabaseField(columnName = "path")
    private String mFSPath;
    @ForeignCollectionField(eager = true, orderColumnName = SketchElement.COLUMN_ORDER)
    private List<SketchElement> elements;


    public Integer getId() {
        return mId;
    }

    public void setId(Integer aId) {
        mId = aId;
    }

    public String getFSPath() {
        return mFSPath;
    }

    public void setFSPath(String aFSPath) {
        mFSPath = aFSPath;
    }

    public List<SketchElement> getElements() {
        return elements;
    }

    public void setElements(List<SketchElement> elements) {
        this.elements = elements;
    }
}
