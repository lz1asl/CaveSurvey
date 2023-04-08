package com.astoev.cave.survey.test.helper

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.astoev.cave.survey.service.ormlite.DatabaseHelper

object Database {

    fun clearDatabase() {
        val ctx = getApplicationContext<Context>()
        ctx!!.deleteDatabase(DatabaseHelper.DATABASE_NAME)
        //        new DatabaseHelper(ctx);
    }
}