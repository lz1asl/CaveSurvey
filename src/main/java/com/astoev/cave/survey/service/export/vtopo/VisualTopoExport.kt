package com.astoev.cave.survey.service.export.vtopo

import android.content.Context
import com.astoev.cave.survey.model.Location
import com.astoev.cave.survey.model.Photo
import com.astoev.cave.survey.model.Project
import com.astoev.cave.survey.model.Sketch
import com.astoev.cave.survey.service.export.AbstractExport
import java.io.InputStream

class VisualTopoExport(aContext: Context?) : AbstractExport(aContext) {


    override fun setValue(entityType: Entities?, aLabel: String?) {
        TODO("Not yet implemented")
    }

    override fun setValue(entityType: Entities?, aValue: Float?) {
        TODO("Not yet implemented")
    }

    override fun setPhoto(aPhoto: Photo?) {
        TODO("Not yet implemented")
    }

    override fun setLocation(aLocation: Location?) {
        TODO("Not yet implemented")
    }

    override fun getContent(): InputStream {
        TODO("Not yet implemented")
    }

    override fun prepare(aProject: Project?) {
        TODO("Not yet implemented")
    }

    override fun prepareEntity(rowCounter: Int) {
        TODO("Not yet implemented")
    }

    override fun setDrawing(aSketch: Sketch?) {
        TODO("Not yet implemented")
    }
}