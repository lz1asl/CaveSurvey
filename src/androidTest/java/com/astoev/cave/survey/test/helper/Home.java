package com.astoev.cave.survey.test.helper;

import static com.astoev.cave.survey.R.id.surveysList;
import static com.astoev.cave.survey.test.helper.Common.checkVisible;

public class Home {

    public static void goHome() {
        // goes directly to the surveys screen
        checkVisible(surveysList);
    }



}
