package com.astoev.cave.survey.util;

import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.service.Workspace;
import com.j256.ormlite.dao.Dao;

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
                newPoint.setName(newName.toUpperCase());
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

    public static Point generateMiddlePoint(Point aPoint) {

        String pointName = aPoint.getName();
        Point point = new Point();

        if (Character.isLetter(pointName.charAt(pointName.length() - 1))) {
            // increase letter index 2A -> 2B
            char letter = pointName.charAt(pointName.length() - 1);
            String newName = pointName.substring(0, pointName.length() - 1) + (char) (letter + 1);
            point.setName(newName.toUpperCase());
            // TODO verify name does not exist
        } else {
            // append letter 2 -> 2A
//            point.setName(aPoint.getName() + MIDDLE_POINT_START_CHAR);
            // TODO verify name does not exist
        }
        return point;
    }


    public static Point createSecondPoint() {
        Point p = new Point();
        p.setName("" + (START_INDEX + 1));
        return p;
    }
}
