package com.astoev.cave.survey.service;

import android.util.Log;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.model.Option;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 2/12/12
 * Time: 12:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class Options {

    public static String getOptionValue(String aCode) {

        Option o = getOption(aCode);
        if (null != o) {
            return o.getValue();
        } else {
            return null;
        }
    }



    public static Option getOption(String aCode) {

        try {
            QueryBuilder<Option, Object> query = Workspace.getCurrentInstance().getDBHelper().getOptionsDao().queryBuilder();
            query.where().eq(Option.COLUMN_CODE, aCode).and().eq(Option.COLUMN_PROJECT_ID, Workspace.getCurrentInstance().getActiveProject().getId());

            return (Option) Workspace.getCurrentInstance().getDBHelper().getOptionsDao().queryForFirst(query.prepare());
        } catch (SQLException e) {
            Log.e(Constants.LOG_TAG_DB, "Failed to get option " + aCode, e);
            return null;
        }
    }

    public static void createOption(String aCode, String aValue) throws SQLException {
        Option o = new Option(aCode, aValue, Workspace.getCurrentInstance().getActiveProject());
        Workspace.getCurrentInstance().getDBHelper().getOptionsDao().create(o);
    }

}
