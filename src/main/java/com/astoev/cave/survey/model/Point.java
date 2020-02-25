package com.astoev.cave.survey.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;


/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/25/12
 * Time: 10:15 AM
 * To change this template use File | Settings | File Templates.
 */
@DatabaseTable(tableName = "points")
public class Point implements Serializable {

	private static final long serialVersionUID = 201312130309L;

	public static final String COLUMN_POINT_NAME = "name";
	public static final String COLUMN_ID = "id";

    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private Integer mId;
    @DatabaseField(canBeNull = false, columnName = COLUMN_POINT_NAME)
    private String mName;

    public Integer getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String aName) {
        mName = aName;
    }

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Point [id=");
		builder.append(mId);
		builder.append(", name=");
		builder.append(mName);
		builder.append("]");
		return builder.toString();
	}
}
