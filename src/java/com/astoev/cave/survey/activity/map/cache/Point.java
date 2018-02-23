package com.astoev.cave.survey.activity.map.cache;

/**
 * Created by astoev on 2/13/18.
 */

public class Point {

    private float x,y;
    private Integer id;
    private String label;

    public Point(float aX, float aY) {
        x = aX;
        y = aY;
    }

    public Point(float aX, float aY, Integer aId) {
        x = aX;
        y = aY;
        id = aId;
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

    @Override
    public String toString() {
        return "Point{" +
                "id=" + id +
                ", label='" + label + '\'' +
                '}';
    }
}
