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
@DatabaseTable(tableName = "points")
public class Gallery implements Serializable {

    public Gallery() {
    }

    @DatabaseField(generatedId = true, columnName = "id")
    private Integer mId;

    @DatabaseField(columnName = "name")
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

}
