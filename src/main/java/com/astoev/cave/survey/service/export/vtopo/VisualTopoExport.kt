package com.astoev.cave.survey.service.export.vtopo

import android.content.Context
import android.util.Log
import com.astoev.cave.survey.Constants
import com.astoev.cave.survey.model.Location
import com.astoev.cave.survey.model.Photo
import com.astoev.cave.survey.model.Project
import com.astoev.cave.survey.model.Sketch
import com.astoev.cave.survey.service.export.AbstractExport
import java.io.ByteArrayInputStream
import java.io.InputStream

class VisualTopoExport(aContext: Context?) : AbstractExport(aContext) {

    val SEPARATOR = ","

    val body = StringBuilder()

    override fun setValue(entityType: Entities?, aLabel: String?) {
        // TODO
    }

    override fun setValue(entityType: Entities?, aValue: Float?) {
        // TODO
    }

    override fun setPhoto(aPhoto: Photo?) {
        // TODO
    }

    override fun setLocation(aLocation: Location?) {
        // TODO
    }

    override fun getContent(): InputStream {
        return ByteArrayInputStream(body.toString().toByteArray());
    }

    override fun prepare(aProject: Project) {
        Log.i(Constants.LOG_TAG_SERVICE, "Start Visual Topo export ")
        body.clear()

        // headers
        body.appendln("Version 5.11")
        body.appendln("Verification 1")
        body.appendln()
        body.append(aProject.name)
                .append(SEPARATOR)
                // TODO coordinate conversion
                .appendln("806.67000,6305.76200,90.00,LT93")
        body.appendln("Entree A0")
        body.appendln("Couleur 255,255,255")
        body.appendln()
        // TODO static data
        // --/--/---- : survey date
        // 13/11/2013 : comment
        // M or A- manual declination
        body.appendln("Param Deca Degd Clino Degd 0.0000 Dir,Dir,Dir Inc Std --/--/---- M ;13/11/2013 - 59;")
        body.appendln()

        Log.i(Constants.LOG_TAG_SERVICE, "Generated body: $body")
    }

    override fun prepareEntity(rowCounter: Int) {
        // nothing really to prepare
    }

    override fun setDrawing(aSketch: Sketch?) {
        // TODO
    }
}