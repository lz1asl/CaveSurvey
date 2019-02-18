package com.astoev.cave.survey.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

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

    public Gallery() {
    }

    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private Integer mId;

    @DatabaseField(foreign = true, canBeNull = false, foreignAutoRefresh = true, columnName = COLUMN_PROJECT_ID)
    private Project mProject;

    @DatabaseField(columnName = COLUMN_NAME)
    private String mName;

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

    public Project getProject() {
        return mProject;
    }

    public void setProject(Project aProject) {
        mProject = aProject;
    }

    @Override
    public String toString() {
        return "Gallery{" +
                "mId=" + mId +
                ", mProject=" + mProject +
                ", mName='" + mName + '\'' +
                '}';
    }
}
