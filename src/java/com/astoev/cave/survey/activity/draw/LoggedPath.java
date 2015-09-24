package com.astoev.cave.survey.activity.draw;

import android.graphics.Path;

import com.astoev.cave.survey.model.SketchPoint;
import com.astoev.cave.survey.util.DaoUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by astoev on 9/24/15.
 */
public class LoggedPath extends Path {

    private List<SketchPoint> mPoints = new ArrayList<SketchPoint>();

    @Override
    public void lineTo(float aX, float aY) {
        super.lineTo(aX, aY);
        mPoints.add(new SketchPoint(null, aX, aY));
    }

    public List<SketchPoint> getPoints() {
        return mPoints;
    }

    public static LoggedPath fromSketchElement(Integer anElementId) throws SQLException {
        LoggedPath path = new LoggedPath();

        List<SketchPoint> points = DaoUtil.getSketchElementPoints(anElementId);
        boolean first = true;
        for (SketchPoint point : points) {
            if (first) {
                //
                path.moveTo(point.getX(), point.getY());
                first = false;
            } else {
                path.lineTo(point.getX(), point.getY());
            }
        }

        return path;
    }
}
