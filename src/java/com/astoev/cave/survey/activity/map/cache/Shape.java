package com.astoev.cave.survey.activity.map.cache;

/**
 * Created by astoev on 2/23/18.
 */

public abstract class Shape {

    private ShapeType type;

    private Integer galleryId;

    public ShapeType getType() {
        return type;
    }

    public void setType(ShapeType aType) {
        type = aType;
    }

    public Integer getGalleryId() {
        return galleryId;
    }

    public void setGalleryId(Integer aGalleryId) {
        galleryId = aGalleryId;
    }
}
