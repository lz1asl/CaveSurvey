package com.astoev.cave.survey.test.service.util;


import androidx.test.core.app.ApplicationProvider;

import com.astoev.cave.survey.util.ConfigUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class ConfigUtilTest {

    @Test
    public void testConfigUtil() {

        ConfigUtil.setContext(ApplicationProvider.getApplicationContext().getApplicationContext());

        // missing
        assertFalse(ConfigUtil.getBooleanProperty("missing"));
        assertEquals(0, (int) ConfigUtil.getIntProperty("missing"));
        assertNull(ConfigUtil.getStringProperty("missing"));

        // default
        assertEquals(true, ConfigUtil.getBooleanProperty("missing", true));
        assertEquals(false, ConfigUtil.getBooleanProperty("missing", false));
        assertEquals(1, (int) ConfigUtil.getIntProperty("missing", 1));

        // set and verify
        ConfigUtil.setIntProperty("intprop", 2);
        assertEquals(2, (int) ConfigUtil.getIntProperty("intprop"));

        ConfigUtil.setBooleanProperty("boolprop", true);
        assertEquals(true, ConfigUtil.getBooleanProperty("boolprop"));

        ConfigUtil.setStringProperty("stringprop", "123");
        assertEquals("123", ConfigUtil.getStringProperty("stringprop"));
    }
}
