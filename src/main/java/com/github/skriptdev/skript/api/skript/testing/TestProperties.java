package com.github.skriptdev.skript.api.skript.testing;

/**
 * Properties related to tests.
 */
public class TestProperties {

    public static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("skript.test.enabled", "false"));
    public static final String TEST_SCRIPTS_FOLDER = System.getProperty("skript.test.scripts", "mods/skript_HySkript/tests");

}
