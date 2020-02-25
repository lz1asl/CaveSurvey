package com.astoev.cave.survey.service.bluetooth;

import com.astoev.cave.survey.Constants;

/**
 * Single measure from device or sensor. Fully defined by value, type, unit and destination.
 */
public class Measure {

    private Constants.Measures mMeasure;
    private Constants.MeasureTypes mMeasureType;
    private Constants.MeasureUnits mMeasureUnit;
    private Float mValue;

    public Measure(){
    }

    public Measure(Constants.MeasureTypes aMeasureType, Constants.MeasureUnits aMeasureUnit, Float aValue) {
        mMeasureType = aMeasureType;
        mMeasureUnit = aMeasureUnit;
        mValue = aValue;
    }

    public Constants.Measures getMeasure() {
        return mMeasure;
    }

    public void setMeasure(Constants.Measures aMeasure) {
        mMeasure = aMeasure;
    }

    public Constants.MeasureTypes getMeasureType() {
        return mMeasureType;
    }

    public void setMeasureType(Constants.MeasureTypes aMeasureType) {
        mMeasureType = aMeasureType;
    }

    public Constants.MeasureUnits getMeasureUnit() {
        return mMeasureUnit;
    }

    public void setMeasureUnit(Constants.MeasureUnits aMeasureUnit) {
        mMeasureUnit = aMeasureUnit;
    }

    public Float getValue() {
        return mValue;
    }

    public void setValue(Float aValue) {
        mValue = aValue;
    }

    @Override
    public String toString() {
        return "Measure{" +
                "mMeasureType=" + mMeasureType +
                ", mMeasureUnit=" + mMeasureUnit +
                ", mValue=" + mValue +
                '}';
    }
}
