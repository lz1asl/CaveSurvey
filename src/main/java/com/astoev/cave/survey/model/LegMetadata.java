package com.astoev.cave.survey.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Leg metadata - extra fields and accuracy info.
 *
 */
@DatabaseTable(tableName = "leg_metadata")
public class LegMetadata implements Serializable {

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_LEG_ID = "leg_id";


    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private Integer mId;

    @DatabaseField(canBeNull = false, foreign = true, columnName = COLUMN_LEG_ID)
    private Leg mLeg;

    @DatabaseField(columnName = "key", canBeNull = false)
    private String mKey;

    @DatabaseField(columnName = "value")
    private String mValue;


    public LegMetadata() {
    }

    public LegMetadata(Leg aLeg, String aKey, String aValue) {
        mLeg = aLeg;
        mKey = aKey;
        mValue = aValue;
    }


    public Integer getId() {
        return mId;
    }

    public void setId(Integer aId) {
        mId = aId;
    }

    public Leg getLeg() {
        return mLeg;
    }

    public void setLeg(Leg aLeg) {
        mLeg = aLeg;
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String aKey) {
        mKey = aKey;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String aValue) {
        mValue = aValue;
    }

    @Override
    public String toString() {
        return mKey + ': ' + mValue;
    }
}