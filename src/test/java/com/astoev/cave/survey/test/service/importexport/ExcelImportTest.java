package com.astoev.cave.survey.test.service.importexport;

import com.astoev.cave.survey.service.imp.ExcelImport;
import com.astoev.cave.survey.service.imp.LegData;
import com.astoev.cave.survey.service.imp.ProjectData;

import junit.framework.TestCase;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.astoev.cave.survey.model.Option.CODE_AZIMUTH_UNITS;
import static com.astoev.cave.survey.model.Option.CODE_DISTANCE_UNITS;
import static com.astoev.cave.survey.model.Option.CODE_SLOPE_UNITS;
import static com.astoev.cave.survey.model.Option.UNIT_DEGREES;
import static com.astoev.cave.survey.model.Option.UNIT_GRADS;
import static com.astoev.cave.survey.model.Option.UNIT_METERS;

public class ExcelImportTest extends TestCase {

    @Test
    public void testSimpleFile() throws IOException {

        // file properly loaded
        ProjectData data = loadExcel("simple_20190220_1");

        // proper units
        assertConfigUnits(data, UNIT_METERS, UNIT_GRADS, UNIT_GRADS);

        // check some data
        List<LegData> legs = data.getLegs();
        assertEquals(1, legs.size());
        assertLeg(legs.get(0), 1f, 2f, 3f);
        assertLegLocation(legs.get(0), null, null, null, null);
    }

    @Test
    public void testImportExportedFile() throws IOException {

        // file properly loaded
        ProjectData data = loadExcel("Stefan_prekop_20190207_1_initial");

        // proper units
        assertDefaultUnits(data);

        // check some data
        List<LegData> legs = data.getLegs();
        assertEquals(105, legs.size());
        assertLeg(legs.get(0), 6f, 231.1f, 0.626f);
        assertLegLocation(legs.get(0), 48.324408f, 17.224757f, 399f, 10f);
        assertLeg(legs.get(1), 11f, 236.4f, 0.98f);
        assertLegLocation(legs.get(1), null, null, null, null);
    }

    @Test
    public void testImportEditedFileWithStringCellValues() throws IOException {

        // file properly loaded
        ProjectData data = loadExcel("Stefan_prekop_20190207_1_stringcells");

        // proper units
        assertDefaultUnits(data);

        // check some data
        List<LegData> legs = data.getLegs();
        assertEquals(106, legs.size());
        assertLeg(legs.get(0), 6f, 231.1f, 0f);
        assertLegLocation(legs.get(0), 48.324408f, 17.224757f, 399f, 10f);
        assertLeg(legs.get(1), 11f, 236.4f, 0f);
        assertLegLocation(legs.get(1), null, null, null, null);
    }


    private void assertLegLocation(LegData aLeg, Float aLat, Float aLon, Float anAlt, Float anAccuracy) {
        assertNotNull(aLeg);
        assertEquals(aLat, aLeg.getLat());
        assertEquals(aLon, aLeg.getLon());
        assertEquals(anAlt, aLeg.getAlt());
        assertEquals(anAccuracy, aLeg.getAccuracy());
    }

    private void assertLeg(LegData aLeg, Float aDistance, Float anAzimuth, Float aSlope) {
        assertNotNull(aLeg);
        assertEquals(aDistance, aLeg.getLength());
        assertEquals(anAzimuth, aLeg.getAzimuth());
        assertEquals(aSlope, aLeg.getSlope());
    }

    private void assertDefaultUnits(ProjectData aData) {
        assertConfigUnits(aData, UNIT_METERS, UNIT_DEGREES, UNIT_DEGREES);
    }

    private void assertConfigUnits(ProjectData aData, String distanceUnit, String azimuthUni, String slopeUnit) {
        assertConfig(aData, CODE_DISTANCE_UNITS, distanceUnit);
        assertConfig(aData, CODE_AZIMUTH_UNITS, azimuthUni);
        assertConfig(aData, CODE_SLOPE_UNITS, slopeUnit);

    }

    private void assertConfig(ProjectData aData, String aProperty, String anExpectedValue) {
        String actualValue = aData.getOptions().get(aProperty);
        assertEquals(anExpectedValue, actualValue);
    }

    private ProjectData loadExcel(String aFile) throws IOException {
        File f = new File("src/test/resources/xls/" + aFile + ".xls");
        ProjectData data = ExcelImport.loadProjectData(f);
        return data;
    }
}
