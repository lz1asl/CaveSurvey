/**
 * 
 */
package com.astoev.cave.survey.util;

/**
 * POJO that sums the Project's information 
 * 
 * @author jivko
 */
public class ProjectInfo {

    /** Project's name*/
    private String name;
    
    /** Creation date as String*/
    private String creationDate;
    
    /** Number of galleries */
    private int galleries;
    
    /** Number of legs*/
    private int legs;
    
    /** Total length */
    private float length;
    
    /** Total depth */
    private float depth;
    
    /** Number of notes*/
    private int notes;
    
    /** Number of gps locations*/
    private int locations;
    
    /** Number of sketches*/
    private int sketches;
    
    /** Number of photos */
    private int photos;
    
    /**
     * Constructor for ProjectInfo
     * 
     * @param nameArg 
     * @param creationDateArg
     * @param galleriesArg
     * @param legsArg
     * @param lengthArg
     * @param depthArg
     */
    public ProjectInfo(String nameArg, String creationDateArg, int galleriesArg, int legsArg, float lengthArg, float depthArg){
        name = nameArg;
        creationDate = creationDateArg;
        galleries = galleriesArg;
        legs = legsArg;
        length = lengthArg;
        depth = depthArg;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameArg the name to set
     */
    public void setName(String nameArg) {
        name = nameArg;
    }

    /**
     * @return the legs
     */
    public int getLegs() {
        return legs;
    }

    /**
     * @param legsArg the legs to set
     */
    public void setLegs(int legsArg) {
        legs = legsArg;
    }

    /**
     * @return the length
     */
    public float getLength() {
        return length;
    }

    /**
     * @param lengthArg the length to set
     */
    public void setLength(float lengthArg) {
        length = lengthArg;
    }

    /**
     * @return the depth
     */
    public float getDepth() {
        return depth;
    }

    /**
     * @param depthArg the depth to set
     */
    public void setDepth(float depthArg) {
        depth = depthArg;
    }

    /**
     * @return the notes
     */
    public int getNotes() {
        return notes;
    }

    /**
     * @param notesArg the notes to set
     */
    public void setNotes(int notesArg) {
        notes = notesArg;
    }

    /**
     * @return the locations
     */
    public int getLocations() {
        return locations;
    }

    /**
     * @param locationsArg the locations to set
     */
    public void setLocations(int locationsArg) {
        locations = locationsArg;
    }

    /**
     * @return the sketches
     */
    public int getSketches() {
        return sketches;
    }

    /**
     * @param sketchesArg the sketches to set
     */
    public void setSketches(int sketchesArg) {
        sketches = sketchesArg;
    }

    /**
     * @return the photos
     */
    public int getPhotos() {
        return photos;
    }

    /**
     * @param photosArg the photos to set
     */
    public void setPhotos(int photosArg) {
        photos = photosArg;
    }

    /**
     * @return the creationDate
     */
    public String getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDateArg the creationDate to set
     */
    public void setCreationDate(String creationDateArg) {
        creationDate = creationDateArg;
    }

    /**
     * @return the galleries
     */
    public int getGalleries() {
        return galleries;
    }

    /**
     * @param galleriesArg the galleries to set
     */
    public void setGalleries(int galleriesArg) {
        galleries = galleriesArg;
    }
    
}
