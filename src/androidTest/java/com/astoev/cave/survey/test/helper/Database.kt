package com.astoev.cave.survey.test.helper

import androidx.test.InstrumentationRegistry.getTargetContext
import com.astoev.cave.survey.service.ormlite.DatabaseHelper

object Database {
    fun clearDatabase() {
        val ctx = getTargetContext()
        ctx!!.deleteDatabase(DatabaseHelper.DATABASE_NAME)
        //        new DatabaseHelper(ctx);
    }
}