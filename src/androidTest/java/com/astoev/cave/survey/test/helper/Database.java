package com.astoev.cave.survey.test.helper;

import android.content.Context;

import static com.astoev.cave.survey.service.ormlite.DatabaseHelper.DATABASE_NAME;
import static com.astoev.cave.survey.test.helper.Common.getTargetContext;

public class Database {

    public static void clearDatabase() {
        Context ctx = getTargetContext();
        ctx.deleteDatabase(DATABASE_NAME);
//        new DatabaseHelper(ctx);
    }
}
