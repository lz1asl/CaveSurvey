package com.astoev.cave.survey.dto;

/**
 * Holds the configuration while creating or updating a project with ProjectFragment.
 *
 * Created by jivko on 15-3-7.
 *
 * @author Jivko Mitrev
 */
public class ProjectConfig {

    private String name;

    private String distanceUnits;

    private String distanceSensor;

    private String azimuthUnits;

    private String azimuthSensor;

    private String slopeUnits;

    private String slopeSensor;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProjectConfig{");
        sb.append("name='").append(name).append('\'');
        sb.append(", distanceUnits='").append(distanceUnits).append('\'');
        sb.append(", distanceSensor='").append(distanceSensor).append('\'');
        sb.append(", azimuthUnits='").append(azimuthUnits).append('\'');
        sb.append(", azimuthSensor='").append(azimuthSensor).append('\'');
        sb.append(", slopeUnits='").append(slopeUnits).append('\'');
        sb.append(", slopeSensor='").append(slopeSensor).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getName() {
        return name;
    }
    public void setName(String nameArg) {
        name = nameArg;
    }

    public String getDistanceUnits() {
        return distanceUnits;
    }
    public void setDistanceUnits(String distanceUnitsArg) {
        distanceUnits = distanceUnitsArg;
    }

    public String getDistanceSensor() {
        return distanceSensor;
    }
    public void setDistanceSensor(String distanceSensorArg) {
        distanceSensor = distanceSensorArg;
    }

    public String getAzimuthUnits() {
        return azimuthUnits;
    }
    public void setAzimuthUnits(String azimuthUnitsArg) {
        azimuthUnits = azimuthUnitsArg;
    }

    public String getAzimuthSensor() {
        return azimuthSensor;
    }
    public void setAzimuthSensor(String azimuthSensorArg) {
        azimuthSensor = azimuthSensorArg;
    }

    public String getSlopeUnits() {
        return slopeUnits;
    }
    public void setSlopeUnits(String slopeUnitsArg) {
        slopeUnits = slopeUnitsArg;
    }

    public String getSlopeSensor() {
        return slopeSensor;
    }
    public void setSlopeSensor(String slopeSensorArg) {
        slopeSensor = slopeSensorArg;
    }
}
