package com.astoev.cave.survey.test.service.data

import com.astoev.cave.survey.model.Option.*
import com.astoev.cave.survey.service.imp.ExcelImport
import com.astoev.cave.survey.service.imp.ProjectData
import com.astoev.cave.survey.test.helper.ExcelTestUtils
import org.junit.Assert
import org.junit.Test
import java.io.IOException

class ExcelImportTest : AbstractExportTest() {

    @Test
    @Throws(IOException::class)
    fun testSimpleFile() {

        // file properly loaded
        val data = loadExcel("simple_20190220_1")

        // proper units
        ExcelTestUtils.assertConfigUnits(data, UNIT_METERS, UNIT_DEGREES, UNIT_DEGREES)

        // check some data
        val legs = data.legs
        Assert.assertEquals(1, legs.size.toLong())
        ExcelTestUtils.assertLeg(legs[0], 1f, 2f, 3f, 1f, 3f, 4f, 2f)
        ExcelTestUtils.assertLegLocation(legs[0], null, null, null, null)
    }

    @Test
    @Throws(IOException::class)
    fun testSimpleFileNonDefaultUnits() {

        // file properly loaded
        val data = loadExcel("simple_20190220_non_default_units")

        // proper units
        ExcelTestUtils.assertConfigUnits(data, UNIT_FEET, UNIT_GRADS, UNIT_GRADS)

        // check some data
        val legs = data.legs
        Assert.assertEquals(1, legs.size.toLong())
        ExcelTestUtils.assertLeg(legs[0], 1f, 2f, 3f, 1f, 3f, 4f, 2f)
        ExcelTestUtils.assertLegLocation(legs[0], null, null, null, null)
    }

    @Test
    @Throws(IOException::class)
    fun testImportExportedFile() {

        // file properly loaded
        val data = loadExcel("Stefan_prekop_20190207_1_initial")

        // proper units
        ExcelTestUtils.assertDefaultUnits(data)

        // check some data
        val legs = data.legs
        Assert.assertEquals(105, legs.size.toLong())
        ExcelTestUtils.assertLeg(legs[0], 6f, 231.1f, 0.626f)
        ExcelTestUtils.assertLegLocation(legs[0], 48.324408, 17.224757, 399.0, 10.0)
        ExcelTestUtils.assertLeg(legs[1], 11f, 236.4f, 0.98f)
        ExcelTestUtils.assertLegLocation(legs[1], null, null, null, null)
    }

    @Test
    @Throws(IOException::class)
    fun testImportEditedFileWithStringCellValues() {

        // file properly loaded
        val data = loadExcel("Stefan_prekop_20190207_1_stringcells")

        // proper units
        ExcelTestUtils.assertDefaultUnits(data)

        // check some data
        val legs = data.legs
        Assert.assertEquals(106, legs.size.toLong())
        ExcelTestUtils.assertLeg(legs[0], 6f, 231.1f, 0f)
        ExcelTestUtils.assertLegLocation(legs[0], 48.324408, 17.224757, 399.0, 10.0)
        ExcelTestUtils.assertLeg(legs[1], 11f, 236.4f, 0f)
        ExcelTestUtils.assertLegLocation(legs[1], null, null, null, null)
    }

    @Throws(IOException::class)
    private fun loadExcel(aFile: String): ProjectData {
        val resourceStream = findAsset("export/xls/$aFile.xls")
        return ExcelImport.loadProjectData(resourceStream)
    }

    @Test
    @Throws(IOException::class)
    fun testImportEmptyRowsAndNumericCoordinates() {

        // file properly loaded
        val data = loadExcel("numeric_coordinate_cells_and_empty_rows")

        // proper units
        ExcelTestUtils.assertConfigUnits(data, UNIT_METERS, UNIT_GRADS, UNIT_GRADS)
        // check some data
        val legs = data.legs
        Assert.assertEquals(3, legs.size.toLong())
        ExcelTestUtils.assertLeg(legs[0], 4.73f, 235f, -3f, 5f, 1f, 4f, 6f)
        ExcelTestUtils.assertLegLocation(legs[0], 41.945457, 24.022383, null, null)
        ExcelTestUtils.assertLeg(legs[1], 5.22f, 135f, -85f)
        ExcelTestUtils.assertLegLocation(legs[1], null, null, null, null)
        ExcelTestUtils.assertLeg(legs[2], 3.1f, 0f, -90f)
        ExcelTestUtils.assertLegLocation(legs[2], null, null, null, null)
    }
}