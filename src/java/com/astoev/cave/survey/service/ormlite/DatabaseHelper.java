package com.astoev.cave.survey.service.ormlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Location;
import com.astoev.cave.survey.model.Note;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.model.Photo;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.model.Sketch;
import com.astoev.cave.survey.model.Vector;
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

    private static final int DATABASE_VERSION_1 = 1;
    private static final int DATABASE_VERSION_2 = 2;
    private static final int DATABASE_VERSION_3 = 3;
    private static final int DATABASE_VERSION_4 = 4;
    private static final int DATABASE_VERSION_LATEST = DATABASE_VERSION_4;
    private static final String DATABASE_NAME = "CaveSurvey";

    private Dao<Leg, Integer> mLegDao;
    private Dao<Location, Integer> mLocationDao;
    private Dao<Note, Integer> mNoteDao;
    private Dao<Photo, Integer> mPhotoDao;
    private Dao<Point, Integer> mPointDao;
    private Dao<Project, Integer> mProjectDao;
    private Dao<Sketch, Integer> mSketchDao;
    private Dao<Gallery, Integer> mGalleryDao;
    private Dao<Option, Integer> mOptionsDao;
    private Dao<Vector, Integer> mVectorsDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION_LATEST, R.raw.ormlite_config);
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
    public void onUpgrade(SQLiteDatabase aSqLiteDatabase, ConnectionSource aConnectionSource, int aOldVersion, int aNewVersion) {

        if (aOldVersion < DATABASE_VERSION_LATEST) {

            Log.i(Constants.LOG_TAG_DB, "Performing DB update...");

            try {
                aSqLiteDatabase.beginTransaction();

                if (aOldVersion < DATABASE_VERSION_2) {
                    Log.i(Constants.LOG_TAG_DB, "Upgrading DB to V2");
                    aSqLiteDatabase.execSQL("alter table legs add column middle_point_distance decimal default null");
                    Log.i(Constants.LOG_TAG_DB, "Upgrade success");
                }

                if (aOldVersion < DATABASE_VERSION_3) {
                    Log.i(Constants.LOG_TAG_DB, "Upgrading DB to V3");
                    aSqLiteDatabase.execSQL("alter table vectors add column gallery_id decimal default null");
                    aSqLiteDatabase.execSQL("update vectors set gallery_id = " +
                            "(select min(gallery_id) from legs where from_point_id = id)");

                    aSqLiteDatabase.execSQL("alter table photos add column gallery_id decimal default null");
                    aSqLiteDatabase.execSQL("update photos set gallery_id = " +
                            "(select min(gallery_id) from legs where from_point_id = id)");

                    aSqLiteDatabase.execSQL("alter table sketches add column gallery_id decimal default null");
                    aSqLiteDatabase.execSQL("update sketches set gallery_id = " +
                            "(select min(gallery_id) from legs where from_point_id = id)");

                    aSqLiteDatabase.execSQL("alter table notes add column gallery_id decimal default null");
                    aSqLiteDatabase.execSQL("update notes set gallery_id = " +
                            "(select min(gallery_id) from legs where from_point_id = id)");

                    aSqLiteDatabase.setTransactionSuccessful();

                    Log.i(Constants.LOG_TAG_DB, "Upgrade success");
                }

                if (aOldVersion < DATABASE_VERSION_4) {
                    Log.i(Constants.LOG_TAG_DB, "Upgrading DB to V3");

                    // can't drop columns in sql lite
//                    aSqLiteDatabase.execSQL("ALTER TABLE sketches DROP COLUMN gallery_id");
//                    aSqLiteDatabase.execSQL("ALTER TABLE sketches DROP COLUMN point_id");

                    // sketchch_points table
                   aSqLiteDatabase.execSQL("create table sketch_element_points (" +
                            "   ID decimal primary key not null," +
                            "   orderby int," +
                            "   x FLOAT," +
                            "   y FLOAT," +
                            "   element_id decimal" +
                            ")");

                    // sketch_elements table
                    aSqLiteDatabase.execSQL("create table sketch_elements (" +
                            "   ID decimal primary key not null," +
                            "   sketch_id decimal not null," +
                            "   orderby int," +
                            "   size int," +
                            "   type varchar," +
                            "   color int" +
                            ")");

                    // project sketch
                    aSqLiteDatabase.execSQL("ALTER TABLE projects create COLUMN sketch_id int");

                    aSqLiteDatabase.setTransactionSuccessful();

                    Log.i(Constants.LOG_TAG_DB, "Upgrade success");
                }


            } finally {
                aSqLiteDatabase.endTransaction();
            }

            Log.i(Constants.LOG_TAG_DB, "End DB update...");
        } else {
            Log.i(Constants.LOG_TAG_DB, "DB up to update");
        }
    }

    public Dao<Leg, Integer> getLegDao() {
        return mLegDao;
    }

    public Dao<Location, Integer> getLocationDao() {
        return mLocationDao;
    }

    public Dao<Note, Integer> getNoteDao() {
        return mNoteDao;
    }

    public Dao<Photo, Integer> getPhotoDao() {
        return mPhotoDao;
    }

    public Dao<Point, Integer> getPointDao() {
        return mPointDao;
    }

    public Dao<Project, Integer> getProjectDao() {
        return mProjectDao;
    }

    public Dao<Sketch, Integer> getSketchDao() {
        return mSketchDao;
    }

    public Dao<Gallery, Integer> getGalleryDao() {
        return mGalleryDao;
    }

    public Dao<Option, Integer> getOptionsDao() {
        return mOptionsDao;
    }

    public Dao<Vector, Integer> getVectorsDao() {
        return mVectorsDao;
    }
}
