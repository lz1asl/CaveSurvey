package com.astoev.cave.survey.test.helper

import com.astoev.cave.survey.test.helper.Common.webViewClick
import java.lang.Thread.sleep

object OpensTopo {

    fun exportCSV() {

        webViewClick("Menu")
        sleep(2000)
        webViewClick("Export to csv")
    }

}