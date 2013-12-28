/**
 *
 */
package com.astoev.cave.survey.util;

import java.sql.SQLException;

import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Note;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.model.Sketch;
import com.astoev.cave.survey.service.Workspace;
import com.j256.ormlite.stmt.QueryBuilder;

/**
 * @author jivko
 */
public class DaoUtil {

    public static Note getActiveLegNote(Leg aActiveLeg) throws SQLException {
        QueryBuilder<Note, Integer> query = Workspace.getCurrentInstance().getDBHelper().getNoteDao().queryBuilder();
        query.where().eq(Note.COLUMN_POINT_ID, aActiveLeg.getFromPoint().getId());
        return (Note) Workspace.getCurrentInstance().getDBHelper().getNoteDao().queryForFirst(query.prepare());
    }

    public static Sketch getScetchByLeg(Leg legArg) throws SQLException {
        return getScetchByPoint(legArg.getFromPoint());
    }

    public static Sketch getScetchByPoint(Point pointArg) throws SQLException {
        QueryBuilder<Note, Integer> query = Workspace.getCurrentInstance().getDBHelper().getSketchDao().queryBuilder();
        query.where().eq(Note.COLUMN_POINT_ID, pointArg.getId());
        return (Sketch) Workspace.getCurrentInstance().getDBHelper().getSketchDao().queryForFirst(query.prepare());
    }

    public static Project getProject(int aId) throws SQLException {
        return (Project) Workspace.getCurrentInstance().getDBHelper().getProjectDao().queryForId(aId);
    }

    public static Leg getLeg(int aId) throws SQLException {
        return (Leg) Workspace.getCurrentInstance().getDBHelper().getLegDao().queryForId(aId);
    }


    public static Point getPoint(Integer aId) throws SQLException {
        return (Point) Workspace.getCurrentInstance().getDBHelper().getPointDao().queryForId(aId);
    }
}
