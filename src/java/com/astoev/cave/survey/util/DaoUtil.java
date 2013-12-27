/**
 * 
 */
package com.astoev.cave.survey.util;

import java.sql.SQLException;

import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Note;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Sketch;
import com.astoev.cave.survey.service.Workspace;
import com.j256.ormlite.stmt.QueryBuilder;

/**
 * @author jivko
 *
 */
public class DaoUtil {

    public static Note getActiveLegNote(Leg aActiveLeg, Workspace aWorkspace) throws SQLException {
        QueryBuilder<Note, Integer> query = aWorkspace.getDBHelper().getNoteDao().queryBuilder();
        query.where().eq(Note.COLUMN_POINT_ID, aActiveLeg.getFromPoint().getId());
        return (Note) aWorkspace.getDBHelper().getNoteDao().queryForFirst(query.prepare());
    }
    
    public static Sketch getScetchByLeg(Leg legArg, Workspace workspaceArg) throws SQLException{
    	return getScetchByPoint(legArg.getFromPoint(), workspaceArg);
    }
    
    public static Sketch getScetchByPoint(Point pointArg, Workspace workspaceArg) throws SQLException{
    	QueryBuilder<Note, Integer> query = workspaceArg.getDBHelper().getSketchDao().queryBuilder();
    	query.where().eq(Note.COLUMN_POINT_ID, pointArg.getId());
    	return (Sketch)workspaceArg.getDBHelper().getSketchDao().queryForFirst(query.prepare());
    }
}
