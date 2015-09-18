package com.astoev.cave.survey.activity.map;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 2/25/13
 * Time: 11:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class Point2D {

    private Float x, y;
    private Float left = 0f, right = 0f;
    private Float angle = 0f;

    public Point2D(Float x, Float y) {
        this.x = x;
        this.y = y;
    }

    public Point2D(Float x, Float y, Float left, Float right, Float angle) {
        this.x = x;
        this.y = y;
        this.left = left;
        this.right = right;
        this.angle = angle;
    }

    public float getX() {
        return x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }

    public Float getLeft() {
        return left;
    }

    public void setLeft(Float left) {
        this.left = left;
    }

    public Float getRight() {
        return right;
    }

    public void setRight(Float right) {
        this.right = right;
    }

    public Float getAngle() {
        return angle;
    }

    public void setAngle(Float angle) {
        this.angle = angle;
    }
}

