package com.astoev.cave.survey.test.helper

import com.astoev.cave.survey.R

object Point {

    fun delete() {
        Common.openContextMenu()
        Common.click("Delete")
    }

    fun reverse() {
        Common.openContextMenu()
        Common.click("Reverse")
        Common.click("YES")
    }

    fun nextLeg() {
        // press new
        Common.click(R.id.main_action_add)

        // select gallery
        Common.clickDialogSpinnerAtPosition(0)
    }
}