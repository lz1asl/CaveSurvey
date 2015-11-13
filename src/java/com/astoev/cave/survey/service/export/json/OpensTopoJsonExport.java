package com.astoev.cave.survey.service.export.json;

import android.content.Context;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.model.Location;
import com.astoev.cave.survey.model.Photo;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.model.Sketch;
import com.astoev.cave.survey.service.export.AbstractExport;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

/**
 * Created by astoev on 8/28/14.
 */
public class OpensTopoJsonExport extends AbstractExport {

    private JSONArray rows;
    private JSONObject project;
    private JSONObject row;

    public OpensTopoJsonExport(Context aContext) {
        super(aContext);
    }

    @Override
    protected void prepare(Project aProject) throws JSONException {
        Log.i(Constants.LOG_TAG_SERVICE, "Start JSON export ");

        project = new JSONObject();
        project.put("name", aProject.getName());
        project.put("altitude", "0");
        project.put("longitude", "0.0");
        project.put("latitude", "0.0");
        project.put("startPoint", "0");
        project.put("geoPoint", "0");
        project.put("northdeclination", "0");

        rows = new JSONArray();
        JSONObject headerRow = new JSONObject();
        // TODO fix units
        headerRow.put("from", null);
        headerRow.put("to", null);
        headerRow.put("len", "m");
        headerRow.put("compass", "deg");
        headerRow.put("clino", "deg");
        headerRow.put("top", "m");
        headerRow.put("left", "m");
        headerRow.put("right", "m");
        headerRow.put("bottom", "m");
        headerRow.put("r", null);

        rows.put(headerRow);
        project.put("data", rows);
    }

    @Override
    protected void prepareEntity(int rowCounter) throws JSONException {

        row = new JSONObject();
        row.put("from", "");
        row.put("to", "");
        row.put("len", "");
        row.put("compass", "");
        row.put("clino", "");
        row.put("top", "");
        row.put("left", "");
        row.put("right", "");
        row.put("bottom", "");
        row.put("r", "");

        rows.put(row);
    }

    @Override
    protected InputStream getContent() {
        String json = project.toString();
        return IOUtils.toInputStream(json);
    }

    @Override
    protected String getExtension() {
        return ".json";
    }

    @Override
    protected void setValue(Entities entityType, String aLabel) throws JSONException {
        populateValue(entityType, aLabel);
    }

    @Override
    protected void setValue(Entities entityType, Float aValue) throws JSONException {
        populateValue(entityType, "" + aValue);
    }

    private void populateValue(Entities entityType, Object aValue) throws JSONException {
        switch (entityType) {
            case FROM:
                row.put("from", aValue);
                break;
            case TO:
                row.put("to", aValue);
                break;
            case DISTANCE:
                row.put("len", aValue);
                break;
            case COMPASS:
                row.put("compass", aValue);
                break;
            case INCLINATION:
                row.put("clino", aValue);
                break;
            case LEFT:
                row.put("left", aValue);
                break;
            case RIGHT:
                row.put("right", aValue);
                break;
            case UP:
                row.put("top", aValue);
                break;
            case DOWN:
                row.put("bottom", aValue);
                break;
            case NOTE:
                row.put("note", aValue);
                break;
        }
    }

    @Override
    protected void setPhoto(Photo photo) {
        // not needed
    }

    @Override
    protected void setLocation(Location aLocation) {
        // not needed
    }

    @Override
    protected void setDrawing(Sketch aSketch) {
        // not needed
    }
}
