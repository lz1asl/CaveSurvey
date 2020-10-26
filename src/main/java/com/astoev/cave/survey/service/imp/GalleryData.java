package com.astoev.cave.survey.service.imp;

import com.astoev.cave.survey.model.GalleryType;

public class GalleryData {

    private String mName;
    private Integer mColor;
    private GalleryType mType;


    public String getName() {
        return mName;
    }

    public void setName(String aName) {
        mName = aName;
    }

    public Integer getColor() {
        return mColor;
    }

    public void setColor(Integer aColor) {
        mColor = aColor;
    }

    public GalleryType getType() {
        return mType;
    }

    public void setType(GalleryType aType) {
        mType = aType;
    }
}
