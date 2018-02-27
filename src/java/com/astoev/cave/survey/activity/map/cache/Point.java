package com.astoev.cave.survey.activity.map.cache;

/**
 * Created by astoev on 2/13/18.
 */

public class Point extends Shape {

    private float x,y;
    private Integer id;
    private String label;
    private boolean current;

    public Point(float aX, float aY) {
        this(aX, aY, null);
    }

    public Point(float aX, float aY, Integer aId) {
        x = aX;
        y = aY;
        id = aId;
        setType(ShapeType.POINT);
    }

    public float getX() {
        return x;
    }

    public void setX(float aX) {
        x = aX;
    }

    public float getY() {
        return y;
    }

    public void setY(float aY) {
        y = aY;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer aId) {
        id = aId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String aLabel) {
        label = aLabel;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean aCurrent) {
        current = aCurrent;
    }

    @Override
    public String toString() {
        return "Point{" +
                "id=" + id +
                ", label='" + label + '\'' +
                '}';
    }
}
