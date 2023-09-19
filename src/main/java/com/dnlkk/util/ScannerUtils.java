package com.dnlkk.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScannerUtils {
    public static List<URL> getResourcesRecursively(String basePackage) throws IOException {
        String basePackagePath = basePackage.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(basePackagePath);

        List<URL> allResources = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File resourceFile = new File(resource.getFile());
            allResources.add(resource);

            if (resourceFile.isDirectory()) {
                List<URL> subResources = scanResourcesInDirectory(resourceFile, basePackagePath);
                allResources.addAll(subResources);
            }
        }

        return allResources;
    }

    private static List<URL> scanResourcesInDirectory(File directory, String basePackagePath) throws IOException {
        List<URL> resources = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    List<URL> subResources = scanResourcesInDirectory(file, basePackagePath);
                    resources.addAll(subResources);
                } else {
                    String filePath = file.getAbsolutePath();
                    if (File.separatorChar != '/') {
                        filePath = filePath.replace(File.separatorChar, '/');
                    }
                    if (filePath.startsWith(basePackagePath)) {
                        filePath = filePath.substring(basePackagePath.length());
                    }
                    resources.add(new URL("file:/" + filePath));
                }
            }
        }
        return resources;
    }

    public static Set<Class<?>> findClassesFromDirectory(String basePackage) throws IOException, ClassNotFoundException {
        String basePackagePath = basePackage.replace('.', '/');
        Set<Class<?>> classes = new HashSet<>();

        List<URL> resources = ScannerUtils.getResourcesRecursively(basePackagePath);
        for (URL resource : resources) {
            if (resource.getProtocol().equals("file")) {
                File file = new File(resource.getFile());
                if (file.isFile() && file.getName().endsWith(".class")) {
                    String className = basePackage + file.getCanonicalPath().replace("\\", ".").split(basePackage)[1].replace(".class", "");
                    Class<?> clazz = Class.forName(className);
                    
                    classes.add(clazz);
                }
            }
        }
            
        return classes;
    }
}