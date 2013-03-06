package com.astoev.cave.survey.activity.map;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 2/25/13
 * Time: 11:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class Point2D {

    private float x, y;
    private float left=0, right=0;
    private float azimuth=0;

    public Point2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Point2D(float x, float y, float left, float right, float azimuth) {
        this.x = x;
        this.y = y;
        this.left = left;
        this.right = right;
        this.azimuth = azimuth;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getLeft() {
        return left;
    }

    public void setLeft(float left) {
        this.left = left;
    }

    public float getRight() {
        return right;
    }

    public void setRight(float right) {
        this.right = right;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }
}

