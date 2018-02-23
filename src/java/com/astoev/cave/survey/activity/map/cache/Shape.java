package com.astoev.cave.survey.activity.map.cache;

/**
 * Created by astoev on 2/23/18.
 */

public abstract class Shape {

    private ShapeType type;

    public ShapeType getType() {
        return type;
    }

    public void setType(ShapeType aType) {
        type = aType;
    }
}
