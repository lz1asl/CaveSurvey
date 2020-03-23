package com.astoev.cave.survey.service.export.vtopo;

import android.content.Context;

import com.astoev.cave.survey.model.Location;
import com.astoev.cave.survey.model.Photo;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.model.Sketch;
import com.astoev.cave.survey.service.export.AbstractExport;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

public class VisualTopoExport extends AbstractExport {

    public VisualTopoExport(Context aContext) {
        super(aContext);
    }

    @Override
    protected void prepare(Project aProject) throws JSONException {

    }

    @Override
    protected void prepareEntity(int rowCounter) throws JSONException {

    }

    @Override
    protected void setValue(Entities entityType, String aLabel) throws JSONException {

    }

    @Override
    protected void setValue(Entities entityType, Float aValue) throws JSONException {

    }

    @Override
    protected void setPhoto(Photo aPhoto) {

    }

    @Override
    protected void setLocation(Location aLocation) throws JSONException {

    }

    @Override
    protected void setDrawing(Sketch aSketch) {

    }

    @Override
    protected InputStream getContent() throws IOException {
        return null;
    }
}
