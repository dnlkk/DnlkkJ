package com.dnlkk.boot;

import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class AppConfig {
    private static Map<String, Object> config;
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    public static String getProperty(String key) {
        if (config == null) {
            loadConfig();
        }
        return findProperty(config, key);
    }

    private static String findProperty(Map<String, Object> config, String key) {
        if (config == null)
            return null;
        if (config.containsKey(key))
            return config.get(key).toString();
        String[] keys = key.split("\\.");
        if (keys.length == 0)
            return null;
        String keyFirst = keys[0];
        return findProperty((Map<String,Object>) config.get(keyFirst), key.substring(keyFirst.length() + 1));
    }

    public static boolean loadConfig() {
        Yaml yaml = new Yaml();
        if (config != null)
            return true;
        try (InputStream inputStream = AppConfig.class.getClassLoader().getResourceAsStream("application.yml")) {
            config = yaml.load(inputStream);
            return config != null;
        } catch (Exception e) {
            logger.error("application.yml config in resources not found");
        }
        return false;
    }
}