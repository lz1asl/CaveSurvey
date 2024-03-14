package com.astoev.cave.survey.test.helper

import com.astoev.cave.survey.R

object Point {

    fun delete() {
        delete(true)
    }

    fun delete(confirm: Boolean) {
        Common.openContextMenu()
        Common.click("Delete")
        confirm(confirm)
    }

    fun reverse() {
        reverse(true)
    }

    fun reverse(confirm: Boolean) {
        Common.openContextMenu()
        Common.click("Reverse")
        confirm(confirm)
    }

    private fun confirm(confirm: Boolean) {
        if (confirm) {
            Common.click("YES")
        } else {
            Common.click("NO")
        }
    }

    fun nextLeg() {
        // press new
        Common.click(R.id.main_action_add)

        // select gallery
        Common.clickDialogSpinnerAtPosition(0)
    }
}