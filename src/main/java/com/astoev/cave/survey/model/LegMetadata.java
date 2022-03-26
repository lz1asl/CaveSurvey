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
    private Float mKey;

    @DatabaseField(columnName = "value")
    private Float mValue;


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

    public Float getKey() {
        return mKey;
    }

    public void setKey(Float aKey) {
        mKey = aKey;
    }

    public Float getValue() {
        return mValue;
    }

    public void setValue(Float aValue) {
        mValue = aValue;
    }
}