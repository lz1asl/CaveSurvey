package com.astoev.cave.survey.test.service.export;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.astoev.cave.survey.activity.home.SplashActivity;
import com.astoev.cave.survey.service.imp.ExcelImport;
import com.astoev.cave.survey.service.imp.LegData;
import com.astoev.cave.survey.service.imp.ProjectData;
import com.astoev.cave.survey.test.helper.Data;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.astoev.cave.survey.model.Option.UNIT_DEGREES;
import static com.astoev.cave.survey.model.Option.UNIT_METERS;
import static com.astoev.cave.survey.sharedtest.export.ExcelTestUtils.assertConfigUnits;
import static com.astoev.cave.survey.sharedtest.export.ExcelTestUtils.assertLeg;
import static com.astoev.cave.survey.test.helper.Common.goBack;
import static com.astoev.cave.survey.test.helper.Data.dataScreen;
import static com.astoev.cave.survey.test.helper.Data.xlsExport;
import static com.astoev.cave.survey.test.helper.Survey.addLeg;
import static com.astoev.cave.survey.test.helper.Survey.addLegMiddle;
import static com.astoev.cave.survey.test.helper.Survey.addVector;
import static com.astoev.cave.survey.test.helper.Survey.createAndOpenSurvey;
import static com.astoev.cave.survey.test.helper.Survey.nextGallery;
import static com.astoev.cave.survey.test.helper.Survey.openLegWithText;
import static com.astoev.cave.survey.test.helper.Survey.saveLeg;
import static com.astoev.cave.survey.test.helper.Survey.selectFirstSurveyLeg;
import static com.astoev.cave.survey.test.helper.Survey.setLegData;
import static org.junit.Assert.assertEquals;

@LargeTest
public class ExcelTest {

    @Rule
    public ActivityTestRule<SplashActivity> activityRule = new ActivityTestRule<>(SplashActivity.class);

    @Rule
    public TestRule permissionRule = GrantPermissionRule.grant(WRITE_EXTERNAL_STORAGE);

    @After
    public void clean() {
//        clearDatabase();
    }

    @Test
    public void testExport() throws IOException {

        // create survey
        String surveyName = createAndOpenSurvey();

        // empty first leg
        List<LegData> legs = exportAndRead(surveyName, 1);
        assertLeg(legs.get(0), null, null, null);
        assertLeg(legs.get(0), "A", "0", "A", "1", false, false);

        // fist minimal and base leg
        selectFirstSurveyLeg();
        setLegData(1f, 2f, null);
        addLeg(1.2f, 2.2f, 1.3f);
        legs = exportAndRead(surveyName, 2);
        assertFirstLegNoSlope(legs);
        assertSecondSimpleLeg(legs);

        // with side measurements
        addLeg(2.3f, 3.4f, 4.5f, 1.1f, 1.2f, 1.3f, 1.4f);
        legs = exportAndRead(surveyName, 3);
        assertFirstLegNoSlope(legs);
        assertSecondSimpleLeg(legs);
        assertThirdWithSidesLeg(legs);

        // with middles
        addLeg(5.5f, 4.4f, 5.5f, 2.1f, 2.2f, 2.3f, 2.4f);
        addLegMiddle(3.1f, 3.1f, 3.2f, 3.3f, 3.4f);
        legs = exportAndRead(surveyName, 6);
        assertFirstLegNoSlope(legs);
        assertSecondSimpleLeg(legs);
        assertThirdWithSidesLeg(legs);
        assertForthWithSidesLeg(legs);
        assertFifthLegWithMiddlePoint(legs);

        // with vectors
        addLeg(3.4f, 3.5f, 3.6f, 6.1f, 6.2f, 6.3f, 6.4f);
        openLegWithText("A5");
        addVector(1.1f, 1.2f, 1.3f);
        addVector(1.4f, 1.5f, 1.6f);
        addVector(1.7f, 1.8f, 1.9f);
        saveLeg();
        legs = exportAndRead(surveyName, 10);
        assertFirstLegNoSlope(legs);
        assertSecondSimpleLeg(legs);
        assertThirdWithSidesLeg(legs);
        assertForthWithSidesLeg(legs);
        assertFifthLegWithMiddlePoint(legs);
        assertSixthLegWithVectors(legs);

        // another gallery
        nextGallery();
        setLegData(4.4f, 4.5f, 4.6f, 0.1f, 0.2f, 0.3f, 0.4f);
        legs = exportAndRead(surveyName, 11);
        assertFirstLegNoSlope(legs);
        assertSecondSimpleLeg(legs);
        assertThirdWithSidesLeg(legs);
        assertForthWithSidesLeg(legs);
        assertFifthLegWithMiddlePoint(legs);
        assertSixthLegWithVectors(legs);
        assertSeventhLegNextGallery(legs);

        // now try to import it back and check the data
        goBack();
        surveyName = createAndOpenSurvey(true);
        legs = exportAndRead(surveyName, 11);
        assertFirstLegNoSlope(legs);
        assertSecondSimpleLeg(legs);
        assertThirdWithSidesLeg(legs);
        assertForthWithSidesLeg(legs);
        assertFifthLegWithMiddlePoint(legs);
        assertSixthLegWithVectors(legs);
        assertSeventhLegNextGallery(legs);
    }


    private List<LegData> exportAndRead(String aSurveyName, int aLegsCount) throws IOException {
        // export
        dataScreen();
        xlsExport();
        goBack();

        // loadTransitionBridgingViewAction
        File exportFile = Data.getLastXlsExport(aSurveyName);
        ProjectData data = ExcelImport.loadProjectData(exportFile);

        // default units
        assertConfigUnits(data, UNIT_METERS, UNIT_DEGREES, UNIT_DEGREES);// expected number of legs

        // expected number of legs
        List<LegData> legs = data.getLegs();
        assertEquals(aLegsCount, legs.size());
        return legs;
    }

    private void assertFirstLegNoSlope(List<LegData> legs) {
        assertLeg(legs.get(0), 1f, 2f, null);
        assertLeg(legs.get(0), "A", "0", "A", "1", false, false);
    }

    private void assertSecondSimpleLeg(List<LegData> legs) {
        assertLeg(legs.get(1), 1.2f, 2.2f, 1.3f);
        assertLeg(legs.get(1), "A", "1", "A", "2", false, false);
    }

    private void assertThirdWithSidesLeg(List<LegData> legs) {
        assertLeg(legs.get(2), 2.3f, 3.4f, 4.5f, 1.1f, 1.2f, 1.3f, 1.4f);
        assertLeg(legs.get(2), "A", "2", "A", "3", false, false);
    }

    private void assertForthWithSidesLeg(List<LegData> legs) {
        assertLeg(legs.get(3), 5.5f, 4.4f, 5.5f, 2.1f, 2.2f, 2.3f, 2.4f);
        assertLeg(legs.get(3), "A", "3", "A", "4", false, false);
    }

    private void assertFifthLegWithMiddlePoint(List<LegData> legs) {
        assertLeg(legs.get(4), 3.1f, 4.4f, 5.5f, 2.1f, 2.2f, 2.3f, 2.4f);
        assertLeg(legs.get(4), "A", "3", "A", "3-4@3.1", true, false);
        assertLeg(legs.get(5), 2.4f, 4.4f, 5.5f, 3.1f, 3.2f, 3.3f, 3.4f);
        assertLeg(legs.get(5), "A", "3-4@3.1", "A", "4", true, false);
    }

    private void assertSixthLegWithVectors(List<LegData> legs) {
        assertLeg(legs.get(6), 3.4f, 3.5f, 3.6f, 6.1f, 6.2f, 6.3f, 6.4f);
        assertLeg(legs.get(6), "A", "4", "A", "5", false, false);
        assertLeg(legs.get(7), 1.1f, 1.2f, 1.3f);
        assertLeg(legs.get(7), "A", "4", null, null, false, true);
        assertLeg(legs.get(8), 1.4f, 1.5f, 1.6f);
        assertLeg(legs.get(8), "A", "4", null, null, false, true);
        assertLeg(legs.get(9), 1.7f, 1.8f, 1.9f);
        assertLeg(legs.get(9), "A", "4", null, null, false, true);
    }

    private void assertSeventhLegNextGallery(List<LegData> legs) {
        assertLeg(legs.get(10), 4.4f, 4.5f, 4.6f, 0.1f, 0.2f, 0.3f, 0.4f);
        assertLeg(legs.get(10), "A", "4", "B", "1", false, false);
    }
}