package com.astoev.cave.survey.test.service.importexport;

import com.astoev.cave.survey.service.imp.ExcelImport;
import com.astoev.cave.survey.service.imp.LegData;
import com.astoev.cave.survey.service.imp.ProjectData;

import junit.framework.TestCase;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.astoev.cave.survey.model.Option.UNIT_GRADS;
import static com.astoev.cave.survey.model.Option.UNIT_METERS;
import static com.astoev.cave.survey.sharedtest.export.ExcelTestUtils.assertConfigUnits;
import static com.astoev.cave.survey.sharedtest.export.ExcelTestUtils.assertDefaultUnits;
import static com.astoev.cave.survey.sharedtest.export.ExcelTestUtils.assertLeg;
import static com.astoev.cave.survey.sharedtest.export.ExcelTestUtils.assertLegLocation;

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

    private ProjectData loadExcel(String aFile) throws IOException {
        File f = new File("src/test/resources/xls/" + aFile + ".xls");
        ProjectData data = ExcelImport.loadProjectData(f);
        return data;
    }
}
