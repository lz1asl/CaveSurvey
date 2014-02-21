package com.astoev.cave.survey.util;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.service.Workspace;

import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 3/30/12
 * Time: 1:14 AM
 * To change this template use File | Settings | File Templates.
 * 1-2-3-4-5
 * 1-A1-a2-a3
 * 2-b1-b2
 *
 */
public class PointUtil {

    private static final int START_INDEX = 0;

    private static final char DELIMITER = '.';


    public static Point createFirstPoint() {
        Point p = new Point();
        p.setName("" + START_INDEX);
        return p;
    }

    public static Point generateNextPoint(Integer aGalleryId) throws SQLException {

        Point lastGalleryPoint = Workspace.getCurrentInstance().getLastGalleryPoint(aGalleryId);

        Point newPoint = new Point();

        String pointName = lastGalleryPoint.getName();

        // TODO no middle point support
        if (pointName.indexOf(DELIMITER) != -1) {

            int delimiterIndex = pointName.indexOf(DELIMITER);
            String pointBaseName = pointName.substring(0, delimiterIndex);

            if (Character.isLetter(pointName.charAt(pointName.length() - 1))) {
                // skip the middle point  2.3A -> 2.4
                String index = pointName.substring(delimiterIndex + 1, pointName.length() - 1);
                String newName = pointBaseName + DELIMITER + (Integer.parseInt(index) + 1);
                newPoint.setName(newName);
            } else {
                // increase after the delimiter 2.3 -> 2.4
                String index = pointName.substring(delimiterIndex + 1);
                String newName = pointBaseName + DELIMITER + (Integer.parseInt(index) + 1);
                newPoint.setName(newName);
            }
        } else {
            if (Character.isLetter(pointName.charAt(pointName.length() - 1))) {
                // increase letter index 2A -> 2B
                char letter = pointName.charAt(pointName.length() - 1);
                String newName = pointName.substring(pointName.length() - 1) + (char) (letter + 1);
                newPoint.setName(newName.toUpperCase(Constants.DEFAULT_LOCALE));
            } else {
                // increase the index 2 -> 3
                newPoint.setName(String.valueOf(Long.parseLong(lastGalleryPoint.getName()) + 1));
            }
        }


        return newPoint;
    }

    public static Point generateDeviationPoint(Point aPoint) {
        Point point = new Point();
        point.setName(aPoint.getName() + DELIMITER + START_INDEX);
        return point;
    }

    public static Point createSecondPoint() {
        Point p = new Point();
        p.setName("" + (START_INDEX + 1));
        return p;
    }
    
    /**
     * Helper method to give the gallery name as string for particular from point
     * 
     * @param pointArg - from point
     * @param galleryId - gallery id
     * @return gallery's name
     * @throws SQLException
     */
    public static String getGalleryNameForFromPoint(Point pointArg, Integer galleryId) throws SQLException{
        Leg prevLeg = DaoUtil.getLegByToPoint(pointArg);
        
        if (galleryId != null) {
            if (prevLeg != null &&  !prevLeg.getGalleryId().equals(galleryId)) {
                return DaoUtil.getGallery(prevLeg.getGalleryId()).getName();
            } else {
                return DaoUtil.getGallery(galleryId).getName();
            }
        } else {
            // fresh leg for new gallery
            return DaoUtil.getGallery(prevLeg.getGalleryId()).getName();
        }
    }
    
    /**
     * Helper method to give the gallery name as string for particular to point
     * 
     * @param galleryId - to point
     * @return gallery's name
     * @throws SQLException
     */
    public static String getGalleryNameForToPoint(Integer galleryId) throws SQLException {
        if (galleryId != null) {
            return DaoUtil.getGallery(galleryId).getName();
        } else {
            // fresh leg for new gallery
            return Gallery.generateNextGalleryName(Workspace.getCurrentInstance().getActiveProjectId());
        }
    }
}
