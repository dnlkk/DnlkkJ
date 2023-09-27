package com.dnlkk.boot;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Banner {
    private static String banner;
    private static final Logger logger = LoggerFactory.getLogger(Banner.class);

    public static String init() {
        try (Scanner scanner = new Scanner(Banner.class.getClassLoader().getResourceAsStream("banner.txt"))) {
            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                stringBuilder.append(line).append("\n");
            }
            banner = stringBuilder.toString();
            banner = inputArgs();
            return banner;
        } catch (Exception e) {
            logger.error("banner.txt in resources not found");
        }
        return null;
    }

    private static String inputArgs() {
        String version = AppConfig.getProperty("app.version");
        if (version != null) {
            String[] versions = version.split("\\.");
            if (versions.length > 0)
                banner = banner.replace("%mj", versions[0]);
            if (versions.length > 1)
                banner = banner.replace("%mi", versions[1]);
            if (versions.length > 2)
                banner = banner.replace("%fx", versions[2]);
            if (versions.length > 3)
                banner = banner.replace("%bn", versions[3]);
        }
        String name = AppConfig.getProperty("app.name");
        if (name != null) 
            banner = banner.replace("%name", name);

        return banner;
    }

    @Override
    public String toString() {
        return banner;
    }
    
}