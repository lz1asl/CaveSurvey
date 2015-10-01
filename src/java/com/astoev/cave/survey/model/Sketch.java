package com.astoev.cave.survey.model;

import com.j256.ormlite.dao.ForeignCollection;
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

    public static final String FIELD_ELEMENTS = "elements";


    @DatabaseField(generatedId = true, columnName = "id")
    private Integer mId;
    @DatabaseField(columnName = "path")
    private String mFSPath;
    @ForeignCollectionField(eager = false, orderColumnName = SketchElement.COLUMN_ORDER)
    private ForeignCollection<SketchElement> elements;


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

    public ForeignCollection<SketchElement> getElements() {
        return elements;
    }

    public void setElements(ForeignCollection<SketchElement> elements) {
        this.elements = elements;
    }
}
