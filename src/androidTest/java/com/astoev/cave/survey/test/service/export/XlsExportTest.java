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

import static com.astoev.cave.survey.model.Option.UNIT_DEGREES;
import static com.astoev.cave.survey.model.Option.UNIT_METERS;
import static com.astoev.cave.survey.sharedtest.export.ExcelTestUtils.assertConfigUnits;
import static com.astoev.cave.survey.sharedtest.export.ExcelTestUtils.assertLeg;
import static com.astoev.cave.survey.test.helper.Data.dataScreen;
import static com.astoev.cave.survey.test.helper.Data.xlsExport;
import static com.astoev.cave.survey.test.helper.Home.goHome;
import static com.astoev.cave.survey.test.helper.Survey.addLeg;
import static com.astoev.cave.survey.test.helper.Survey.createSurvey;
import static com.astoev.cave.survey.test.helper.Survey.openSurvey;
import static com.astoev.cave.survey.test.helper.Survey.selectFirstSurveyLeg;
import static com.astoev.cave.survey.test.helper.Survey.setLegData;
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
    public void testExportSurvey() throws IOException {

        // create survey
        final String surveyName = createAndOpenSurvey();

        // add data
        selectFirstSurveyLeg();
        setLegData(1, 2, null);
        addLeg(1.2f, 2.2f, 1.2f);

        // export
        dataScreen();
        xlsExport();

        // verify
        List<File> excelExportFiles = FileStorageUtil.listProjectFiles(null, ExcelExport.EXCEL_FILE_EXTENSION);
        File exportFile = excelExportFiles.stream().filter(file -> file.getName().startsWith(surveyName)).findFirst().get();

        ProjectData data = ExcelImport.loadProjectData(exportFile);

        assertConfigUnits(data, UNIT_METERS, UNIT_DEGREES, UNIT_DEGREES);
        List<LegData> legs = data.getLegs();
        assertEquals(2, legs.size());
        assertLeg(legs.get(0), 1f, 2f, null);
        assertLeg(legs.get(1), 1.2f, 2.2f, null);
    }

    public String createAndOpenSurvey() {
        final String surveyName = "xls" + System.currentTimeMillis();
        goHome();
        createSurvey(surveyName);
        openSurvey(surveyName);
        return surveyName;
    }
}
