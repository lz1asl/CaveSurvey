package com.astoev.cave.survey.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 4/16/12
 * Time: 10:22 PM
 * To change this template use File | Settings | File Templates.
 */
@DatabaseTable(tableName = "options")
public class Option implements Serializable {

	private static final long serialVersionUID = 201401022246L;
	
	public static final String CODE_DISTANCE_UNITS = "distance";
    public static final String CODE_AZIMUTH_UNITS = "azimuth";
    public static final String CODE_SLOPE_UNITS = "slope";

    public static final String CODE_AZIMUTH_SENSOR = "azimuth_sensor";
    public static final String CODE_SLOPE_SENSOR = "slope_sensor";
    public static final String CODE_DISTANCE_SENSOR = "distance_sensor";

    public static final String CODE_SENSOR_INTERNAL = "sensor_internal";
    public static final String CODE_SENSOR_BLUETOOTH = "sensor_bluetooth";
    public static final String CODE_SENSOR_NONE = "sensor_none";

    public static final String UNIT_METERS = "meters";
    public static final String UNIT_FEET = "feet";
    public static final String UNIT_DEGREES = "degrees";
    public static final String UNIT_GRADS = "grads";

    public static final int MAX_VALUE_AZIMUTH_DEGREES = 360;
    public static final int VALUE_AZIMUTH_180_DEGREES = 180;
    public static final int VALUE_AZIMUTH_90_DEGREES = 90;
    public static final int VALUE_AZIMUTH_270_DEGREES = 270;
    public static final int MAX_VALUE_AZIMUTH_GRADS = 400;
    public static final int MIN_VALUE_AZIMUTH = 0;
    public static final int MAX_VALUE_SLOPE_DEGREES = 90;
    public static final int MIN_VALUE_SLOPE_DEGREES = -90;
    public static final int MAX_VALUE_SLOPE_GRADS = 100;
    public static final int MIN_VALUE_SLOPE_GRADS = -100;

    public static final String COLUMN_CODE = "code";
    public static final String COLUMN_PROJECT_ID = "project_id";

    @DatabaseField(generatedId = true, columnName = "id")
    private Integer mId;
    @DatabaseField(foreign = true, canBeNull = false, foreignAutoRefresh = true, columnName = COLUMN_PROJECT_ID)
    private Project mProject;
    @DatabaseField(columnName = COLUMN_CODE)
    private String mCode;
    @DatabaseField(columnName = "value")
    private String mValue;
    @DatabaseField(columnName = "note")
    private String mNote;

    public Option() {
    }

    public Option(String aCode, String aValue, Project aProject) {
        mCode = aCode;
        mValue = aValue;
        mProject = aProject;
    }

    public Integer getId() {
        return mId;
    }

    public void setId(Integer aId) {
        mId = aId;
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String aCode) {
        mCode = aCode;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String aValue) {
        mValue = aValue;
    }

    public String getNote() {
        return mNote;
    }

    public void setNote(String aNote) {
        mNote = aNote;
    }

    public Project getmProject() {
        return mProject;
    }

    public void setmProject(Project mProject) {
        this.mProject = mProject;
    }
}
