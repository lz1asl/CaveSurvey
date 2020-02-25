package com.astoev.cave.survey.service.ormlite;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/24/12
 * Time: 11:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {
    public static void main(String[] args) throws Exception {
        writeConfigFile("ormlite_config.txt");
    }
}
