package com.astoev.cave.survey.test.service.export;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.astoev.cave.survey.activity.home.SplashActivity;
import com.astoev.cave.survey.service.export.excel.ExcelExport;
import com.astoev.cave.survey.service.imp.ExcelImport;
import com.astoev.cave.survey.service.imp.LegData;
import com.astoev.cave.survey.service.imp.ProjectData;
import com.astoev.cave.survey.util.FileStorageUtil;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.astoev.cave.survey.test.helper.Data.dataScreen;
import static com.astoev.cave.survey.test.helper.Data.xlsExport;
import static com.astoev.cave.survey.test.helper.Home.goHome;
import static com.astoev.cave.survey.test.helper.Survey.addLeg;
import static com.astoev.cave.survey.test.helper.Survey.createSurvey;
import static com.astoev.cave.survey.test.helper.Survey.openSurvey;
import static org.junit.Assert.assertEquals;

public class XlsExportTest {

    @Rule
    public ActivityTestRule<SplashActivity> activityRule = new ActivityTestRule<>(SplashActivity.class);


    @After
    public void cleanUp() {
//        clearDatabase();
    }

    @Test
    @LargeTest
    public void testCreateSurvey() throws IOException {

        // create survey
        final String surveyName = "xls" + System.currentTimeMillis();
        goHome();
        createSurvey(surveyName);
        openSurvey(surveyName);

        // add data
        addLeg(1, 2);
        addLeg(1.2f, 2.2f);

        // export
        dataScreen();
        xlsExport();

        // verify
        List<File> excelExportFiles = FileStorageUtil.listProjectFiles(null, ExcelExport.EXCEL_FILE_EXTENSION);
        File exportFile = excelExportFiles.stream().filter(file -> file.getName().startsWith(surveyName)).findFirst().get();

        ProjectData data = ExcelImport.loadProjectData(exportFile);

        // TODO options
        List<LegData> legs = data.getLegs();
        assertEquals(2, legs.size());
        assertLeg(legs.get(0), 1, 2);
        assertLeg(legs.get(1), 1.2f, 2.2f);
    }

    private void assertLeg(LegData leg, float length, float azimuth) {
        assertEquals(length, leg.getLength(), 0.001);
        assertEquals(azimuth, leg.getAzimuth(), 0.001);
    }
}
