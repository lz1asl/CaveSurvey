package com.astoev.cave.survey.service.ormlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.model.*;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/24/12
 * Time: 10:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "CaveSurvey";

    private static DatabaseHelper instance = null;

    private Dao mLegDao;
    private Dao mLocationDao;
    private Dao mNoteDao;
    private Dao mPhotoDao;
    private Dao mPointDao;
    private Dao mProjectDao;
    private Dao mSketchDao;
    private Dao mGalleryDao;
    private Dao mOptionsDao;
    private Dao mVectorsDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
        Log.i(Constants.LOG_TAG_DB, "Initialize DatabaseHelper");
        try {
            mLegDao = getDao(Leg.class);
            mLocationDao = getDao(Location.class);
            mNoteDao = getDao(Note.class);
            mPhotoDao = getDao(Photo.class);
            mPointDao = getDao(Point.class);
            mProjectDao = getDao(Project.class);
            mSketchDao = getDao(Sketch.class);
            mGalleryDao = getDao(Gallery.class);
            mOptionsDao = getDao(Option.class);
            mVectorsDao = getDao(Vector.class);

            Log.i(Constants.LOG_TAG_DB, "Dao's created");
        } catch (SQLException e) {
            Log.e(Constants.LOG_TAG_DB, "Failed to initialize DatabaseHelper", e);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            Log.i(Constants.LOG_TAG_DB, "Create DB tables");
            TableUtils.createTableIfNotExists(connectionSource, Project.class);
            TableUtils.createTableIfNotExists(connectionSource, Note.class);
            TableUtils.createTableIfNotExists(connectionSource, Photo.class);
            TableUtils.createTableIfNotExists(connectionSource, Location.class);
            TableUtils.createTableIfNotExists(connectionSource, Sketch.class);
            TableUtils.createTableIfNotExists(connectionSource, Point.class);
            TableUtils.createTableIfNotExists(connectionSource, Leg.class);
            TableUtils.createTableIfNotExists(connectionSource, Gallery.class);
            TableUtils.createTableIfNotExists(connectionSource, Option.class);
            TableUtils.createTableIfNotExists(connectionSource, Vector.class);
            Log.i(Constants.LOG_TAG_DB, "Tables created");
        } catch (SQLException e) {
            Log.e(Constants.LOG_TAG_DB, "Failed to create DB tables", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {
        Log.e(Constants.LOG_TAG_DB, "Unsupported upgrade to v " + i + ":" + i1);
        throw new RuntimeException("Update not supported yet");
    }

    public Dao getLegDao() {
        return mLegDao;
    }

    public Dao getLocationDao() {
        return mLocationDao;
    }

    public Dao getNoteDao() {
        return mNoteDao;
    }

    public Dao getPhotoDao() {
        return mPhotoDao;
    }

    public Dao getPointDao() {
        return mPointDao;
    }

    public Dao getProjectDao() {
        return mProjectDao;
    }

    public Dao getSketchDao() {
        return mSketchDao;
    }

    public Dao getGalleryDao() {
        return mGalleryDao;
    }

    public Dao getOptionsDao() {
        return mOptionsDao;
    }

    public Dao getVectorsDao() {
        return mVectorsDao;
    }
}
