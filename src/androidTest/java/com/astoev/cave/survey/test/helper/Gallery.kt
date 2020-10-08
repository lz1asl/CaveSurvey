package com.astoev.cave.survey.test.helper;

import static com.astoev.cave.survey.R.string.new_gallery_create;
import static com.astoev.cave.survey.test.helper.Common.click;

public class Gallery {

    public static void createDefaultGallery() {
        // first georeferencing leg
        click(new_gallery_create);
    }
}
