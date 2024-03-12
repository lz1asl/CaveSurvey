package com.astoev.cave.survey.test.service.data

import com.astoev.cave.survey.model.Option.UNIT_GRADS
import com.astoev.cave.survey.model.Option.UNIT_METERS
import com.astoev.cave.survey.test.helper.Common.checkVisible
import com.astoev.cave.survey.test.helper.Point
import com.astoev.cave.survey.test.helper.Point.nextLeg
import com.astoev.cave.survey.test.helper.Survey
import org.junit.Test
import java.io.IOException

class MainActivityTest : AbstractUiTest() {


    @Test
    @Throws(IOException::class)
    fun legNameGenerationTest() {

        // first leg A0->A1
        val name = Survey.createAndOpenSurvey()
        checkVisible("A0->A1")
        Survey.setLegData(1f, 2f, null)

        // A1->A2
        Survey.openSurvey(name)
        nextLeg()
        checkVisible("A1->A2")
        Survey.setLegData(1.2f, 2.2f, null)

        // A2->A3
        nextLeg()
        checkVisible("A2->A3")
        Survey.setLegData(1.2f, 2.2f, null)

        // first gallery A2->B1
        Survey.nextGallery()
        checkVisible("A2->B1")
        Survey.setLegData(3f, 3f, null)

        // first gallery B1->B2
        nextLeg()
        checkVisible("B1->B2")
        Survey.setLegData(3f, 3f, null)

        // third gallery B1->C1
        Survey.nextGallery()
        checkVisible("B1->C1")
        Survey.setLegData(3f, 3f, null)

        // delete last leg, gallery removed as well
        Survey.openLegWithText("C1")
        Point.delete()

        // next gallery will reuse C
        Survey.nextGallery()
        checkVisible("B1->C1")
        Survey.setLegData(3f, 3f, null)

        nextLeg()
        checkVisible("C1->C2")
        Survey.setLegData(3f, 3f, null)

        // redo leg
        Survey.openLegWithText("C2")
        Point.delete()
        nextLeg()
        checkVisible("C1->C2")
    }

    @Test
    @Throws(IOException::class)
    fun reverseDegreesTest() {

        // add leg and reverse
        val name = Survey.createAndOpenSurvey()
        Survey.setLegData(11.1f, 22.2f, 33.3f)
        Survey.openSurvey(name)
        Survey.openLegWithText("A1")
        Point.reverse()

        // check
        checkVisible("11.1")
        checkVisible("202.2")
        checkVisible("-33.3")
    }

    @Test
    @Throws(IOException::class)
    fun reverseGradsTest() {

        // add leg and reverse
        val name = Survey.createAndOpenSurvey(null, UNIT_METERS, UNIT_GRADS, UNIT_GRADS)
        Survey.setLegData(11.1f, 22.2f, -33.3f)
        Survey.openSurvey(name)
        Survey.openLegWithText("A1")
        Point.reverse()

        // check
        checkVisible("11.1")
        checkVisible("222.2")
        checkVisible("33.3")
    }
}