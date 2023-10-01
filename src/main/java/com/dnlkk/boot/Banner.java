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
        String version = AppConfig.getBaseProperty("app.version");
        String versionName = AppConfig.getBaseProperty("app.version_name");

        banner = banner.replace("%ver", version);
        banner = banner.replace("%vname", versionName);

        if (AppConfig.configIsLoaded()) {
            String projectVersion = AppConfig.getProperty("app.version");
            String projectName = AppConfig.getProperty("app.name");

            if (projectVersion == null && projectName == null) {
                banner = banner.replace(":: %pname v%pver ::", "");
            } else {
                if (projectVersion != null && projectName != null)
                    banner = banner.replace("%pname v%pver", projectName + " " + projectVersion);
                else banner = banner.replace("%pname v%pver", Objects.requireNonNullElse(projectVersion, projectName));
            }

        }

        return banner;
    }

    @Override
    public String toString() {
        return banner;
    }

}