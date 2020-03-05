package com.astoev.cave.survey.test.helper;

import android.content.Context;

import static com.astoev.cave.survey.service.ormlite.DatabaseHelper.DATABASE_NAME;
import static com.astoev.cave.survey.test.helper.Common.getContext;

public class Database {

    public static void clearDatabase() {
        Context ctx = getContext();
        ctx.deleteDatabase(DATABASE_NAME);
//        new DatabaseHelper(ctx);
    }
}
