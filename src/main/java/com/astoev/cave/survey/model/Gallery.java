package com.astoev.cave.survey.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import static com.j256.ormlite.field.DataType.ENUM_STRING;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 3/31/12
 * Time: 9:47 PM
 * To change this template use File | Settings | File Templates.
 */
@DatabaseTable(tableName = "galleries")
public class Gallery implements Serializable {

	private static final long serialVersionUID = 201312130309L;
	
    public static final String COLUMN_PROJECT_ID = "project_id";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_COLOR = "color";

    public Gallery() {
    }

    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private Integer mId;

    @DatabaseField(foreign = true, canBeNull = false, foreignAutoRefresh = true, columnName = COLUMN_PROJECT_ID)
    private Project mProject;

    @DatabaseField(columnName = COLUMN_NAME)
    private String mName;

    @DatabaseField(columnName = COLUMN_COLOR)
    private Integer mColor;

    @DatabaseField(columnName = COLUMN_TYPE, dataType = ENUM_STRING)
    private GalleryType mType;

    public Integer getId() {
        return mId;
    }

    public void setId(Integer aId) {
        mId = aId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String aName) {
        mName = aName;
    }

    public Integer getColor() {
        return mColor;
    }

    public void setColor(Integer aColor) {
        mColor = aColor;
    }

    public Project getProject() {
        return mProject;
    }

    public void setProject(Project aProject) {
        mProject = aProject;
    }

    public GalleryType getType() {
        return mType;
    }

    public void setType(GalleryType type) {
        this.mType = type;
    }

    @Override
    public String toString() {
        return "Gallery{" +
                "mId=" + mId +
                ", mProject=" + mProject +
                ", mName='" + mName + '\'' +
                ", type=" + mType +
                '}';
    }
}
