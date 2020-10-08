package com.astoev.cave.survey.test.helper

import com.astoev.cave.survey.R.string
import com.astoev.cave.survey.test.helper.Common.click

object Gallery {

    fun createDefaultGallery() {
        // first georeferencing leg
        click(string.new_gallery_create)
    }
}