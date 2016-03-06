package com.astoev.cave.survey.activity.map;

/**
 * View on the map.
 *
 * Created by astoev on 3/6/16.
 */
public class MapViewPosition {

    public int moveX = 0;
    public int moveY = 0;
    public int scale = 0;

    public MapViewPosition(int moveX, int moveY, int scale) {
        this.moveX = moveX;
        this.moveY = moveY;
        this.scale = scale;
    }
}
