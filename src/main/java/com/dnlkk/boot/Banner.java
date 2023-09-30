package com.dnlkk.boot;

import java.util.Objects;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Banner {
    private static String banner;
    private static final Logger logger = LoggerFactory.getLogger(Banner.class);

    public static String init(Class<?> clazz) {
        try (Scanner scanner = new Scanner(Objects.requireNonNull((clazz == null ? Banner.class : clazz).getClassLoader().getResourceAsStream(
                (AppConfig.configIsLoaded() ? "banner.txt" : "base_banner.txt")
        )))) {
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
        String version = "";
        String verisonName = "";
        String projectName = "";
        if (AppConfig.configIsLoaded()) {
            version = AppConfig.getProperty("app.version");
            verisonName = AppConfig.getProperty("app.version_name");
            if (verisonName == null)
                verisonName = "";
            projectName = AppConfig.getProperty("app.project_name");
            if (projectName == null)
                projectName = "";
        } else {
            version = AppConfig.getBaseProperty("app.version");
            verisonName = AppConfig.getBaseProperty("app.version_name");
        }

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

        banner = banner.replace("%vname", verisonName);
        banner = banner.replace("%pname", projectName);

        return banner;
    }

    @Override
    public String toString() {
        return banner;
    }

}