package com.astoev.cave.survey.test.helper

import com.astoev.cave.survey.R.id

object Home {
    fun goHome() {
        // goes directly to the surveys screen
        Common.checkVisible(id.surveysList)
    }
}