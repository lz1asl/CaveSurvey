/**
 *
 */
package com.astoev.cave.survey.util;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.dto.ProjectConfig;
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
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.service.ormlite.DatabaseHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author jmitrev
 */
public class DaoUtil {

    public static Note getActiveLegNote(Leg anActiveLeg) throws SQLException {
        if (anActiveLeg.isNew()) {
            return null;
        }

        QueryBuilder<Note, Integer> query = Workspace.getCurrentInstance().getDBHelper().getNoteDao().queryBuilder();
        query.where().eq(Note.COLUMN_POINT_ID, anActiveLeg.getFromPoint().getId()).and().eq(Note.COLUMN_GALLERY_ID, anActiveLeg.getGalleryId());
        return Workspace.getCurrentInstance().getDBHelper().getNoteDao().queryForFirst(query.prepare());
    }

    public static Sketch getScetchByLeg(Leg legArg) throws SQLException {
        QueryBuilder<Sketch, Integer> query = Workspace.getCurrentInstance().getDBHelper().getSketchDao().queryBuilder();
        query.where().eq(Sketch.COLUMN_POINT_ID, legArg.getFromPoint().getId()).and().eq(Sketch.COLUMN_GALLERY_ID, legArg.getGalleryId());
        return Workspace.getCurrentInstance().getDBHelper().getSketchDao().queryForFirst(query.prepare());
    }

    public static Photo getPhotoByLeg(Leg aLeg) throws SQLException {

        QueryBuilder<Photo, Integer> query = Workspace.getCurrentInstance().getDBHelper().getPhotoDao().queryBuilder();
        query.where().eq(Photo.COLUMN_POINT_ID, aLeg.getFromPoint().getId()).and()
                .eq(Photo.COLUMN_GALLERY_ID, aLeg.getGalleryId());
        return Workspace.getCurrentInstance().getDBHelper().getPhotoDao().queryForFirst(query.prepare());

    }

    public static List<Sketch> getAllSketchesByPoint(Point pointArg) throws SQLException {
        QueryBuilder<Sketch, Integer> query = Workspace.getCurrentInstance().getDBHelper().getSketchDao().queryBuilder();
        query.where().eq(Sketch.COLUMN_POINT_ID, pointArg.getId());
        return Workspace.getCurrentInstance().getDBHelper().getSketchDao().query(query.prepare());
    }


    public static List<Photo> getAllPhotosByPoint(Point pointArg) throws SQLException {
        QueryBuilder<Photo, Integer> query = Workspace.getCurrentInstance().getDBHelper().getPhotoDao().queryBuilder();
        query.where().eq(Photo.COLUMN_POINT_ID, pointArg.getId());
        return Workspace.getCurrentInstance().getDBHelper().getPhotoDao().query(query.prepare());
    }

    public static Location getLocationByPoint(Point pointArg) throws SQLException {
        Dao<Location, Integer> locationDao = Workspace.getCurrentInstance().getDBHelper().getLocationDao();
        QueryBuilder<Location, Integer> query = locationDao.queryBuilder();
        query.where().eq(Location.COLUMN_POINT_ID, pointArg.getId());
        return locationDao.queryForFirst(query.prepare());
    }

    public static Project getProject(int aId) throws SQLException {
        return Workspace.getCurrentInstance().getDBHelper().getProjectDao().queryForId(aId);
    }

    public static Leg getLeg(int aId) throws SQLException {
        return Workspace.getCurrentInstance().getDBHelper().getLegDao().queryForId(aId);
    }


    public static Point getPoint(Integer aId) throws SQLException {
        return Workspace.getCurrentInstance().getDBHelper().getPointDao().queryForId(aId);
    }

    public static Point getPoint(Integer aProjectId, Gallery aGallery, String aPointName) throws SQLException {

        QueryBuilder<Leg, Integer> galleryLegs = Workspace.getCurrentInstance().getDBHelper().getLegDao().queryBuilder();
        galleryLegs.where().eq(Leg.COLUMN_GALLERY_ID, aGallery.getId()).and().eq(Leg.COLUMN_PROJECT_ID, aProjectId);
        QueryBuilder<Point, Integer> points = Workspace.getCurrentInstance().getDBHelper().getPointDao().queryBuilder();
        // TODO joined only by from point
        return points.join(galleryLegs).where().eq(Point.COLUMN_POINT_NAME, aPointName).queryForFirst();
    }

    public static Gallery getGallery(Integer aId) throws SQLException {
        return Workspace.getCurrentInstance().getDBHelper().getGalleryDao().queryForId(aId);
    }

    public static Gallery getGallery(Integer aProjectId, String aGalleryName) throws SQLException {
        return Workspace.getCurrentInstance().getDBHelper().getGalleryDao().queryBuilder()
                .where().eq(Gallery.COLUMN_PROJECT_ID, aProjectId).and().eq(Gallery.COLUMN_NAME, aGalleryName).queryForFirst();
    }

    public static List<Leg> getCurrProjectLegs(boolean includeMiddles) throws SQLException {
        return getProjectLegs(Workspace.getCurrentInstance().getActiveProjectId(), includeMiddles);
    }

    public static List<Leg> getProjectLegs(Integer aProjectId, boolean includeMiddles) throws SQLException {
        QueryBuilder<Leg, Integer> query = Workspace.getCurrentInstance().getDBHelper().getLegDao().queryBuilder();
        Where<Leg, Integer> where = query.where().eq(Leg.COLUMN_PROJECT_ID, aProjectId);
        if (!includeMiddles) {
            where.and().isNull(Leg.COLUMN_MIDDLE_POINT_AT_DISTANCE);
        }

        query.orderBy(Leg.COLUMN_GALLERY_ID, true);
        query.orderBy(Leg.COLUMN_FROM_POINT, true);
        query.orderBy(Leg.COLUMN_TO_POINT, true);

        if (includeMiddles) {
            query.orderBy(Leg.COLUMN_MIDDLE_POINT_AT_DISTANCE, true);
        }

        return Workspace.getCurrentInstance().getDBHelper().getLegDao().query(query.prepare());
    }

    public static void refreshPoint(Point aPoint) throws SQLException {
        Workspace.getCurrentInstance().getDBHelper().getPointDao().refresh(aPoint);
    }

    public static Gallery createGallery(boolean isFirst) throws SQLException {
        Project currProject = Workspace.getCurrentInstance().getActiveProject();
        String name;
        if (isFirst) {
            name = Gallery.getFirstGalleryName();
        } else {
            name = Gallery.generateNextGalleryName(currProject.getId());
        }
        return createGallery(currProject, name);
    }

    public static Gallery createGallery(Project aProject, String aName) throws SQLException {
        Gallery gallery = new Gallery();
        gallery.setName(aName);
        gallery.setProject(aProject);
        Workspace.getCurrentInstance().getDBHelper().getGalleryDao().create(gallery);
        return gallery;
    }

    public static Gallery getLastGallery(Integer aProjectId) throws SQLException {
        QueryBuilder<Gallery, Integer> query = Workspace.getCurrentInstance().getDBHelper().getGalleryDao().queryBuilder();
        query.where().eq(Gallery.COLUMN_PROJECT_ID, aProjectId);
        query.orderBy(Gallery.COLUMN_ID, false);
        return query.queryForFirst();
    }

    public static Leg getLegByToPointId(long aToPointId) throws SQLException {
        // TODO this will work as soon as we keep a tree of legs. Once we start closing circles will break and will have to change the logic
        QueryBuilder<Leg, Integer> query = Workspace.getCurrentInstance().getDBHelper().getLegDao().queryBuilder();
        query.where().eq(Leg.COLUMN_TO_POINT, aToPointId).and().isNull(Leg.COLUMN_MIDDLE_POINT_AT_DISTANCE);
        return query.queryForFirst();
    }

    // previous leg in same gallery or null
    public static Leg getGalleryPrevLeg(Leg aLeg) throws SQLException {
        QueryBuilder<Leg, Integer> query = Workspace.getCurrentInstance().getDBHelper().getLegDao().queryBuilder();
        query.where().eq(Leg.COLUMN_TO_POINT, aLeg.getFromPoint().getId()).and().isNull(Leg.COLUMN_MIDDLE_POINT_AT_DISTANCE)
                .and().eq(Leg.COLUMN_GALLERY_ID, aLeg.getGalleryId());
        return query.queryForFirst();
    }

    // next leg in same gallery or null
    public static Leg getGalleryNextLeg(Leg aLeg) throws SQLException {
        QueryBuilder<Leg, Integer> query = Workspace.getCurrentInstance().getDBHelper().getLegDao().queryBuilder();
        query.where().eq(Leg.COLUMN_FROM_POINT, aLeg.getToPoint().getId()).and().isNull(Leg.COLUMN_MIDDLE_POINT_AT_DISTANCE)
                .and().eq(Leg.COLUMN_GALLERY_ID, aLeg.getGalleryId());
        return query.queryForFirst();
    }

    public static List<Leg> getLegsMiddles(Leg aLeg) throws SQLException {
        QueryBuilder<Leg, Integer> query = Workspace.getCurrentInstance().getDBHelper().getLegDao().queryBuilder();
        query.where().eq(Leg.COLUMN_FROM_POINT, aLeg.getFromPoint().getId()).and().eq(Leg.COLUMN_GALLERY_ID, aLeg.getGalleryId())
                .and().isNotNull(Leg.COLUMN_MIDDLE_POINT_AT_DISTANCE);
        query.orderBy(Leg.COLUMN_MIDDLE_POINT_AT_DISTANCE, true);
        return query.query();
    }

    public static boolean hasLegsByFromPoint(Point aFromPoint) throws SQLException {
        QueryBuilder<Leg, Integer> query = Workspace.getCurrentInstance().getDBHelper().getLegDao().queryBuilder();
        query.where().eq(Leg.COLUMN_FROM_POINT, aFromPoint.getId());
        return query.countOf() > 0;
    }

    public static long getGalleriesCount(Integer aActiveProjectId) throws SQLException {
        QueryBuilder<Gallery, Integer> query = Workspace.getCurrentInstance().getDBHelper().getGalleryDao().queryBuilder();
        query.where().eq(Gallery.COLUMN_PROJECT_ID, aActiveProjectId);
        return query.countOf();
    }

    /**
     * DAO method that saves or update the location of Point based on GPS location
     *
     * @param parentPointArg - parent Point
     * @param gpsLocationArg - GPS Location
     * @throws SQLException if there is a problem working with the DB
     */
    public static void saveLocationToPoint(final Point parentPointArg, final android.location.Location gpsLocationArg)
            throws SQLException {

        ConnectionSource connetionSource = Workspace.getCurrentInstance().getDBHelper().getConnectionSource();
        TransactionManager.callInTransaction(connetionSource, new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                Location oldLocation = getLocationByPoint(parentPointArg);
                if (oldLocation != null) {
                    oldLocation.setLatitude(gpsLocationArg.getLatitude());
                    oldLocation.setLongitude(gpsLocationArg.getLongitude());
                    oldLocation.setAltitude((int) gpsLocationArg.getAltitude());
                    oldLocation.setAccuracy((int) gpsLocationArg.getAccuracy());
                    Workspace.getCurrentInstance().getDBHelper().getLocationDao().update(oldLocation);

                    Log.i(Constants.LOG_TAG_DB, "Update location with id:" + oldLocation.getId() + " for point:" + parentPointArg.getId());
                    return oldLocation.getId();
                } else {
                    Location newLocation = new Location();
                    newLocation.setPoint(parentPointArg);
                    newLocation.setLatitude(gpsLocationArg.getLatitude());
                    newLocation.setLongitude(gpsLocationArg.getLongitude());
                    newLocation.setAltitude((int) gpsLocationArg.getAltitude());
                    newLocation.setAccuracy((int) gpsLocationArg.getAccuracy());
                    Workspace.getCurrentInstance().getDBHelper().getLocationDao().create(newLocation);

                    Log.i(Constants.LOG_TAG_DB, "Creted location with id:" + newLocation.getId() + " for point:" + parentPointArg.getId());
                    return newLocation.getId();
                }
            }
        });
    }

    /**
     * DAO method that saves or update the location of Point based on manual Location
     *
     * @param parentPointArg - parent Point
     * @param gpsLocationArg - GPS Location
     * @throws SQLException if there is a problem working with the DB
     */
    public static void saveLocationToPoint(final Point parentPointArg, final Location gpsLocationArg)
            throws SQLException {

        ConnectionSource connetionSource = Workspace.getCurrentInstance().getDBHelper().getConnectionSource();
        TransactionManager.callInTransaction(connetionSource, new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                Location oldLocation = getLocationByPoint(parentPointArg);
                if (oldLocation != null) {
                    oldLocation.setLatitude(gpsLocationArg.getLatitude());
                    oldLocation.setLongitude(gpsLocationArg.getLongitude());
                    oldLocation.setAltitude((int) gpsLocationArg.getAltitude());
                    oldLocation.setAccuracy((int) gpsLocationArg.getAccuracy());
                    Workspace.getCurrentInstance().getDBHelper().getLocationDao().update(oldLocation);

                    Log.i(Constants.LOG_TAG_DB, "Update location with id:" + oldLocation.getId() + " for point:" + parentPointArg.getId());
                    return oldLocation.getId();
                } else {
                    gpsLocationArg.setPoint(parentPointArg);
                    Workspace.getCurrentInstance().getDBHelper().getLocationDao().create(gpsLocationArg);

                    Log.i(Constants.LOG_TAG_DB, "Creted location with id:" + gpsLocationArg.getId() + " for point:" + parentPointArg.getId());
                    return gpsLocationArg.getId();
                }
            }
        });
    }

    /**
     * Deletes a leg its toPoint and all the data that is related to toPoint
     *
     * @param aLegToDelete - leg to delete
     * @return true if the leg is successfully deleted
     * @throws SQLException if there is a problem with DB
     */
    public static boolean deleteLeg(final Leg aLegToDelete) throws SQLException {
        if (aLegToDelete.isNew()) {
            return false;
        }

        final Workspace workspace = Workspace.getCurrentInstance();
        final DatabaseHelper dbHelper = Workspace.getCurrentInstance().getDBHelper();

        TransactionManager.callInTransaction(dbHelper.getConnectionSource(), new Callable<Object>() {
            public Object call() throws Exception {
                Log.d(Constants.LOG_TAG_DB, "Deleting " + workspace.getActiveLegId());

                if (aLegToDelete.isMiddle()) {

                    // delete middle leg
                    int deletedLeg = dbHelper.getLegDao().delete(aLegToDelete);
                    Log.d(Constants.LOG_TAG_DB, "Deleted middle leg:" + deletedLeg);

                    workspace.setActiveLeg(getLegByToPointId(aLegToDelete.getToPoint().getId()));
                } else {

                    Point toPoint = aLegToDelete.getToPoint();

                    // delete note
                    Note note = getActiveLegNote(aLegToDelete);
                    if (note != null) {
                        int deleted = dbHelper.getNoteDao().delete(note);
                        Log.d(Constants.LOG_TAG_DB, "Deleted note:" + deleted);
                    }

                    // delete location
                    Location location = getLocationByPoint(toPoint);
                    if (location != null) {
                        int deleted = dbHelper.getLocationDao().delete(location);
                        Log.d(Constants.LOG_TAG_DB, "Deleted location:" + deleted);
                    }

                    // delete photos
                    List<Photo> photosList = getAllPhotosByPoint(toPoint);
                    if (photosList != null && !photosList.isEmpty()) {
                        int deleted = dbHelper.getPhotoDao().delete(photosList);
                        Log.d(Constants.LOG_TAG_DB, "Deleted photos:" + deleted);
                    }

                    // delete sketches
                    List<Sketch> sketchList = getAllSketchesByPoint(toPoint);
                    if (sketchList != null && !sketchList.isEmpty()) {
                        int deleted = dbHelper.getSketchDao().delete(sketchList);
                        Log.d(Constants.LOG_TAG_DB, "Deleted sketches:" + deleted);
                    }

                    List<Vector> legVectors = getLegVectors(aLegToDelete);
                    if (legVectors != null) {
                        for (Vector v : legVectors) {
                            Log.d(Constants.LOG_TAG_DB, "Deleting vector:" + v.getId());
                            deleteVector(v);
                        }

                    }

                    // delete leg
                    int deletedLeg = dbHelper.getLegDao().delete(aLegToDelete);
                    Log.d(Constants.LOG_TAG_DB, "Deleted leg:" + deletedLeg);


                    // delete middle points
                    List<Leg> legMiddles = getLegsMiddles(aLegToDelete);
                    if (legMiddles != null) {
                        for (Leg l : legMiddles) {
                            Log.d(Constants.LOG_TAG_DB, "Deleting middle:" + l.getId());
                            dbHelper.getLegDao().delete(l);
                        }
                    }

                    // delete to point
                    int deletedPoint = dbHelper.getPointDao().delete(toPoint);
                    Log.d(Constants.LOG_TAG_DB, "Deleted point:" + deletedPoint);

                    workspace.setActiveLeg(workspace.getLastLeg());
                }

                return null;
            }
        });
        return true;
    }

    /**
     * Creates an ProjectInfo that sums up the project
     *
     * @return ProjectInfo
     * @throws SQLException if there is an DB problem
     */
    public static ProjectInfo getProjectInfo() throws SQLException {

        List<Leg> legs = DaoUtil.getCurrProjectLegs(false);
        Project project = Workspace.getCurrentInstance().getActiveProject();
        String name = project.getName();
        String creationDate = StringUtils.dateToDateTimeString(project.getCreationDate());

        float totalLength = 0, totalDepth = 0;
        int numNotes = 0, numDrawings = 0, numCoordinates = 0, numPhotos = 0, numVectors = 0;
        for (Leg l : legs) {

            // TODO calculate the correct distance and depth
            if (l.getDistance() != null) {
                totalLength += l.getDistance();
            }

            // notes
            if (DaoUtil.getActiveLegNote(l) != null) {
                numNotes++;
            }

            Point fromPoint = l.getFromPoint();

            // drawings
            List<Sketch> sketchesList = DaoUtil.getAllSketchesByPoint(fromPoint);
            if (sketchesList != null && !sketchesList.isEmpty()) {
                numDrawings += sketchesList.size();
            }

            // gps
            Location locaiton = DaoUtil.getLocationByPoint(fromPoint);
            if (locaiton != null) {
                numCoordinates++;
            }

            // photos
            List<Photo> photos = DaoUtil.getAllPhotosByPoint(fromPoint);
            if (photos != null && !photos.isEmpty()) {
                numPhotos += photos.size();
            }

            // vectors
            List<Vector> vectors = DaoUtil.getLegVectors(l);
            if (vectors != null && !vectors.isEmpty()) {
                numVectors += vectors.size();
            }
        }

        int numGalleries = (int) DaoUtil.getGalleriesCount(project.getId());

        ProjectInfo projectInfo = new ProjectInfo(name, creationDate, numGalleries, legs.size(), totalLength, totalDepth);

        projectInfo.setNotes(numNotes);
        projectInfo.setSketches(numDrawings);
        projectInfo.setLocations(numCoordinates);
        projectInfo.setPhotos(numPhotos);
        projectInfo.setVectors(numVectors);

        return projectInfo;
    }

    /**
     * Helper method that reads active project's configuration
     *
     * @return ProjectConfig
     */
    public static ProjectConfig getProjectConfig(){
        ProjectConfig config = new ProjectConfig();

        Project project = Workspace.getCurrentInstance().getActiveProject();
        String name = project.getName();

        String distanceUnits = Options.getOptionValue(Option.CODE_DISTANCE_UNITS);
        String distanceSensor = Options.getOptionValue(Option.CODE_DISTANCE_SENSOR);
        String azimuthUnits = Options.getOptionValue(Option.CODE_AZIMUTH_UNITS);
        String azimuthSensor = Options.getOptionValue(Option.CODE_AZIMUTH_SENSOR);
        String slopeUnits = Options.getOptionValue(Option.CODE_SLOPE_UNITS);
        String slopeSensor = Options.getOptionValue(Option.CODE_SLOPE_SENSOR);

        config.setName(name);
        config.setDistanceUnits(distanceUnits);
        config.setDistanceSensor(distanceSensor);
        config.setAzimuthUnits(azimuthUnits);
        config.setAzimuthSensor(azimuthSensor);
        config.setSlopeUnits(slopeUnits);
        config.setSlopeSensor(slopeSensor);

        return config;
    }

    public static List<Vector> getLegVectors(Leg aLeg) throws SQLException {
        QueryBuilder<Vector, Integer> vectorsQuery = Workspace.getCurrentInstance().getDBHelper().getVectorsDao().queryBuilder();
        vectorsQuery.where().eq(Vector.COLUMN_POINT, aLeg.getFromPoint().getId()).and().eq(Vector.COLUMN_GALLERY_ID, aLeg.getGalleryId());
        vectorsQuery.orderBy(Vector.COLUMN_ID, true);
        return vectorsQuery.query();
    }

    public static boolean hasVectorsByLeg(Leg aLeg) throws SQLException {
        QueryBuilder<Vector, Integer> vectorsQuery = Workspace.getCurrentInstance().getDBHelper().getVectorsDao().queryBuilder();
        vectorsQuery.where().eq(Vector.COLUMN_POINT, aLeg.getFromPoint().getId()).and().eq(Vector.COLUMN_GALLERY_ID, aLeg.getGalleryId());
        return vectorsQuery.countOf() > 0;
    }

    public static void saveVector(Vector aVector) throws SQLException {
        Workspace.getCurrentInstance().getDBHelper().getVectorsDao().create(aVector);
    }

    public static void deleteVector(Vector aVector) throws SQLException {
        Workspace.getCurrentInstance().getDBHelper().getVectorsDao().delete(aVector);
    }

    public static boolean deleteProject(final Integer aProjectId) {

        Log.i(Constants.LOG_TAG_DB, "Delete project " + aProjectId);


        try {
            TransactionManager.callInTransaction(Workspace.getCurrentInstance().getDBHelper().getConnectionSource(), new Callable<Object>() {
                public Object call() throws Exception {
                    List<Leg> legs = getProjectLegs(aProjectId, false);
                    if (legs != null) {
                        for (Leg l : legs) {
                            // delete photos
                            List<Photo> photos = getAllPhotosByPoint(l.getFromPoint());
                            if (photos != null && photos.size() > 0) {
                                for (Photo p : photos) {
                                    FileUtils.deleteQuietly(new File(p.getFSPath()));
                                    Workspace.getCurrentInstance().getDBHelper().getPhotoDao().delete(p);
                                }
                            }

                            // delete sketches
                            List<Sketch> sketches = getAllSketchesByPoint(l.getFromPoint());
                            if (sketches != null && sketches.size() > 0) {
                                for (Sketch s : sketches) {
                                    FileUtils.deleteQuietly(new File(s.getFSPath()));
                                    Workspace.getCurrentInstance().getDBHelper().getSketchDao().delete(s);
                                }
                            }

                            // delete vectors
                            List<Vector> vectors = getLegVectors(l);
                            if (vectors != null && vectors.size() > 0) {
                                Workspace.getCurrentInstance().getDBHelper().getVectorsDao().delete(vectors);
                            }

                            // delete middle points
                            List<Leg> legMiddles = getLegsMiddles(l);
                            if (legMiddles != null) {
                                for (Leg m : legMiddles) {
                                    Log.d(Constants.LOG_TAG_DB, "Deleting middle:" + m.getId());
                                    Workspace.getCurrentInstance().getDBHelper().getLegDao().delete(m);
                                }
                            }

                            // delete locations
                            Location location = getLocationByPoint(l.getFromPoint());
                            if (location != null) {
                                Workspace.getCurrentInstance().getDBHelper().getLocationDao().delete(location);
                            }

                            // delete points
                            Workspace.getCurrentInstance().getDBHelper().getPointDao().delete(l.getFromPoint());
                            Workspace.getCurrentInstance().getDBHelper().getPointDao().delete(l.getToPoint());

                            // delete leg
                            Workspace.getCurrentInstance().getDBHelper().getLegDao().delete(l);
                        }
                    }

                    // delete galleries
                    Gallery g;
                    while ((g = DaoUtil.getLastGallery(aProjectId)) != null) {
                        DaoUtil.deleteGallery(g);
                    }

                    // delete project
                    Project p = getProject(aProjectId);
                    Workspace.getCurrentInstance().getDBHelper().getProjectDao().delete(p);

                    FileUtils.deleteQuietly(FileStorageUtil.getProjectHome(p.getName()));

                    Log.i(Constants.LOG_TAG_DB, "Deleted project " + aProjectId);

                    return null;
                }
            });
        } catch (SQLException e) {
            Log.e(Constants.LOG_TAG_DB, "Failed to delete project", e);
            return false;
        }

        return true;
    }

    private static void deleteGallery(Gallery aG) throws SQLException {
        Workspace.getCurrentInstance().getDBHelper().getGalleryDao().delete(aG);
    }
}
