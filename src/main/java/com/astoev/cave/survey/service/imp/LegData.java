package com.astoev.cave.survey.service.imp;

public class LegData {
    private String fromGallery, fromPoint;
    private String toGallery, toPoint;

    private boolean vector, middlePoint;

    private Float length, azimuth, slope;
    private Float up, down, left, right;

    private Double lat, lon, alt, accuracy;

    private String note;

    private String sketch;
    private String photo;


    public String getFromGallery() {
        return fromGallery;
    }

    public void setFromGallery(String aFromGallery) {
        fromGallery = aFromGallery;
    }

    public String getFromPoint() {
        return fromPoint;
    }

    public void setFromPoint(String aFromPoint) {
        fromPoint = aFromPoint;
    }

    public String getToGallery() {
        return toGallery;
    }

    public void setToGallery(String aToGallery) {
        toGallery = aToGallery;
    }

    public String getToPoint() {
        return toPoint;
    }

    public void setToPoint(String aToPoint) {
        toPoint = aToPoint;
    }

    public boolean isVector() {
        return vector;
    }

    public void setVector(boolean aVector) {
        vector = aVector;
    }

    public boolean isMiddlePoint() {
        return middlePoint;
    }

    public void setMiddlePoint(boolean aMiddlePoint) {
        middlePoint = aMiddlePoint;
    }

    public Float getLength() {
        return length;
    }

    public void setLength(Float aLength) {
        length = aLength;
    }

    public Float getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(Float aAzimuth) {
        azimuth = aAzimuth;
    }

    public Float getSlope() {
        return slope;
    }

    public void setSlope(Float aSlope) {
        slope = aSlope;
    }

    public Float getUp() {
        return up;
    }

    public void setUp(Float aUp) {
        up = aUp;
    }

    public Float getDown() {
        return down;
    }

    public void setDown(Float aDown) {
        down = aDown;
    }

    public Float getLeft() {
        return left;
    }

    public void setLeft(Float aLeft) {
        left = aLeft;
    }

    public Float getRight() {
        return right;
    }

    public void setRight(Float aRight) {
        right = aRight;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double aLat) {
        lat = aLat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double aLon) {
        lon = aLon;
    }

    public Double getAlt() {
        return alt;
    }

    public void setAlt(Double aAlt) {
        alt = aAlt;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double aAccuracy) {
        accuracy = aAccuracy;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String aNote) {
        note = aNote;
    }

    public String getSketch() {
        return sketch;
    }

    public void setSketch(String aSketch) {
        sketch = aSketch;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String aPhoto) {
        photo = aPhoto;
    }
}
