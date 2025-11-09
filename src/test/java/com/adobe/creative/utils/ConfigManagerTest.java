package com.adobe.creative.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigManager.
 * 
 * @author Ugur Köysüren
 */
class ConfigManagerTest {

    @Test
    void testGetPropertyFromPropertiesFile() {
        String outputPath = ConfigManager.getProperty("output.base.path", "default");
        assertNotNull(outputPath);
        assertEquals("assets/output", outputPath);
    }

    @Test
    void testGetPropertyWithDefault() {
        String value = ConfigManager.getProperty("nonexistent.property", "default_value");
        assertEquals("default_value", value);
    }

    @Test
    void testGetBooleanProperty() {
        boolean logoRequired = ConfigManager.getBoolean("brand.logo.required", false);
        assertTrue(logoRequired);
    }

    @Test
    void testGetBooleanWithDefault() {
        boolean value = ConfigManager.getBoolean("nonexistent.boolean", true);
        assertTrue(value);
    }

    @Test
    void testGetIntWithNullPropKey() {
        int value = ConfigManager.getInt("NONEXISTENT_ENV_VAR", null, 42);
        assertEquals(42, value);
    }

    @Test
    void testGetWithNullPropKey() {
        String value = ConfigManager.get("NONEXISTENT_ENV_VAR", null, "default");
        assertEquals("default", value);
    }

    @Test
    void testGetEnvOnly() {
        String value = ConfigManager.getEnv("NONEXISTENT_ENV", "fallback");
        assertEquals("fallback", value);
    }
}

