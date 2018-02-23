package com.astoev.cave.survey.activity.map.cache;

/**
 * Created by astoev on 2/13/18.
 */

public class Line extends Shape {

    private Point from, to;

    public Line(Point aFrom, Point aTo) {
        from = aFrom;
        to = aTo;
    }

    public Point getFrom() {
        return from;
    }

    public void setFrom(Point aFrom) {
        from = aFrom;
    }

    public Point getTo() {
        return to;
    }

    public void setTo(Point aTo) {
        to = aTo;
    }

    @Override
    public String toString() {
        return "Line{" +
                "from=" + from +
                ", to=" + to +
                '}';
    }
}
