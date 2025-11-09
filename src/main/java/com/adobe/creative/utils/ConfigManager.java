package com.adobe.creative.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Manages application configuration from properties file with environment variable overrides.
 * Reads defaults from application.properties and allows override via environment variables.
 * 
 * @author Ugur Köysüren
 */
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final Properties properties = new Properties();
    
    static {
        try (InputStream input = ConfigManager.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
                logger.info("Loaded configuration from application.properties");
            } else {
                logger.warn("application.properties not found, using defaults");
            }
        } catch (IOException e) {
            logger.error("Error loading application.properties: {}", e.getMessage());
        }
    }

    /**
     * Gets a configuration value, checking environment variables first, then properties file.
     * 
     * @param envKey Environment variable name
     * @param propKey Properties file key (can be null if only env var is checked)
     * @param defaultValue Default value if neither is set
     * @return Configuration value
     */
    public static String get(String envKey, String propKey, String defaultValue) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        if (propKey != null) {
            return properties.getProperty(propKey, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Gets a configuration value from properties file only (no env override).
     * 
     * @param key Properties file key
     * @param defaultValue Default value if not set
     * @return Configuration value
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gets an integer configuration value.
     * 
     * @param envKey Environment variable name
     * @param propKey Properties file key
     * @param defaultValue Default value if neither is set
     * @return Integer configuration value
     */
    public static int getInt(String envKey, String propKey, int defaultValue) {
        String value = get(envKey, propKey, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for {}/{}: {}, using default: {}", 
                envKey, propKey, value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Gets a boolean configuration value.
     * 
     * @param key Properties file key
     * @param defaultValue Default value if not set
     * @return Boolean configuration value
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Gets environment variable only (no properties fallback).
     * 
     * @param key Environment variable name
     * @param defaultValue Default value if not set
     * @return Environment variable value or default
     */
    public static String getEnv(String key, String defaultValue) {
        return System.getenv().getOrDefault(key, defaultValue);
    }
}

