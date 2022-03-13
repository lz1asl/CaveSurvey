package com.astoev.cave.survey.service.export.csv

import android.content.res.Resources
import android.util.Log
import com.astoev.cave.survey.Constants
import com.astoev.cave.survey.model.*
import com.astoev.cave.survey.service.Options.getOptionValue
import com.astoev.cave.survey.service.export.AbstractDataExport
import com.astoev.cave.survey.service.export.ExportEntityType
import com.astoev.cave.survey.util.StreamUtil
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.IOException
import java.io.OutputStream
import java.io.StringWriter

/**
 * Exports the project's data as csv file
 *
 * @author astoev
 */
class CsvExport(aResources: Resources?) : AbstractDataExport(aResources) {
    private var buff = StringWriter()
    private var csvPrinter = CSVPrinter(buff, CSVFormat.DEFAULT)


    public override fun getExtension(): String {
        return CSV_FILE_EXTENSION
    }

    override fun getMimeType(): String {
        return CSV_MIME_TYPE
    }

    override fun prepare(aProject: Project) {
        try {
            Log.i(Constants.LOG_TAG_SERVICE, "CSV export preparing")
            buff = StringWriter()
            val format = CSVFormat.DEFAULT.builder()
                .setHeader("From", "To", "Length", "Compass", "Clino", "Left",
                        "Right", "Top", "Bottom", "I", "Note")
                .build()
            csvPrinter = CSVPrinter(buff, format)
            createUnitsRow()
        } catch (aE: IOException) {
            throw RuntimeException(aE)
        }
    }

    override fun prepareEntity(rowCounter: Int, type: ExportEntityType) {
        try {
            csvPrinter.println()
        } catch (aE: IOException) {
            throw RuntimeException(aE)
        }
    }

    override fun writeTo(aProject: Project?, aStream: OutputStream?) {
        csvPrinter.println()
        csvPrinter.close(true)
        StreamUtil.write(buff.toString().toByteArray(), aStream)
    }

    override fun setValue(entityType: Entities, aLabel: String?) {
        try {
            csvPrinter.print(aLabel)
        } catch (aE: IOException) {
            throw RuntimeException(aE)
        }
    }

    override fun setValue(entityType: Entities, aValue: Float?) {
        try {
            csvPrinter.print(aValue)
        } catch (aE: IOException) {
            throw RuntimeException(aE)
        }
    }

    override fun setPhoto(photo: Photo) {
        // not relevant
    }

    override fun setLocation(aLocation: Location) {
        // not relevant
    }

    override fun setDrawing(aSketch: Sketch) {
        // not relevant
    }

    private fun createUnitsRow() {
        try {

            csvPrinter.print(null)
            csvPrinter.print(null)
            csvPrinter.print(getOptionValue(Option.CODE_DISTANCE_UNITS))
            csvPrinter.print(getOptionValue(Option.CODE_AZIMUTH_UNITS))
            csvPrinter.print(getOptionValue(Option.CODE_SLOPE_UNITS))
            csvPrinter.print("m")
            csvPrinter.print("m")
            csvPrinter.print("m")
            csvPrinter.print("m")
            csvPrinter.print(null)
            csvPrinter.print(null)
        } catch (ioe: IOException) {
            throw RuntimeException(ioe)
        }
    }

    companion object {
        const val CSV_FILE_EXTENSION = ".csv"
        const val CSV_MIME_TYPE = "text/csv"
    }
}