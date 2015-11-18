package com.astoev.cave.survey.activity.draw;

import android.graphics.Path;

import com.astoev.cave.survey.model.SketchElement;
import com.astoev.cave.survey.model.SketchPoint;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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

    public static LoggedPath fromSketchElement(SketchElement anElement, float moveX, float moveY, Integer scale) throws SQLException {
        LoggedPath path = new LoggedPath();

        Collection<SketchPoint> points = anElement.getPoints();
        boolean first = true;
        float realScale = 1;// TODO scale / MapView.INITIAL_SCALE;
        for (SketchPoint point : points) {
            if (first) {
                path.moveTo(moveX + point.getX() * realScale, moveY + point.getY() * realScale);
                first = false;
            } else {
                path.lineTo(moveX + point.getX(), moveY + point.getY());
            }
        }

        return path;
    }
}
