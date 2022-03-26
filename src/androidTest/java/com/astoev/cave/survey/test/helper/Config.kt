package com.astoev.cave.survey.test.helper

import com.astoev.cave.survey.test.helper.Common.click
import com.astoev.cave.survey.test.helper.Common.openContextMenu

object Config {

    fun openSettings() {
        openContextMenu()
        click("Settings")
    }

    fun openMeasurements() {
        click("Measurements")
    }
}