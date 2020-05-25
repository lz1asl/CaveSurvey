package com.astoev.cave.survey.test.service.data;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.astoev.cave.survey.service.imp.ExcelImport;
import com.astoev.cave.survey.service.imp.LegData;
import com.astoev.cave.survey.service.imp.ProjectData;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.astoev.cave.survey.model.Option.UNIT_GRADS;
import static com.astoev.cave.survey.model.Option.UNIT_METERS;
import static com.astoev.cave.survey.test.helper.ExcelTestUtils.assertConfigUnits;
import static com.astoev.cave.survey.test.helper.ExcelTestUtils.assertDefaultUnits;
import static com.astoev.cave.survey.test.helper.ExcelTestUtils.assertLeg;
import static com.astoev.cave.survey.test.helper.ExcelTestUtils.assertLegLocation;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExcelImportTest {

    @Test
    public void testSimpleFile() throws IOException {

        // file properly loaded
        ProjectData data = loadExcel("simple_20190220_1");

        // proper units
        assertConfigUnits(data, UNIT_METERS, UNIT_GRADS, UNIT_GRADS);

        // check some data
        List<LegData> legs = data.getLegs();
        assertEquals(1, legs.size());
        assertLeg(legs.get(0), 1f, 2f, 3f, 1f, 3f, 4f, 2f);
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
        InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream("/export/xls/" + aFile + ".xls");
        ProjectData data = ExcelImport.loadProjectData(resourceStream);
        return data;
    }
}
