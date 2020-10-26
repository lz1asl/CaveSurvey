package com.astoev.cave.survey.test.helper

import com.astoev.cave.survey.R
import com.astoev.cave.survey.test.helper.Common.click

object Gallery {

    fun createGallery() {
        // first georeferencing gallery by default
        create();
    }

    fun createClassicGallery() {
        click(R.id.new_gallery_type)
        Common.clickDialogSpinnerAtPosition(1)
        create()
    }

    fun createDefaultGallery() {
        click(R.id.main_action_add)
        click("Next Gallery")
        create()
    }

    private fun create() {
        click("CREATE")
    }

}