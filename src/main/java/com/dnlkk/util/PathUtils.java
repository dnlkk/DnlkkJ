package com.dnlkk.util;

import com.dnlkk.controller.annotations.PathVar;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathUtils {
    public static String[] splitPath(String splitter, String path) {
        String[] keys = path.split(splitter);
        return keys;
    }

    public static boolean isRequestMapping(String requestPath, String methodPath) {
        String[] methodPaths = splitPath("/", methodPath);
        String[] requestPaths = splitPath("/", requestPath);

        if (methodPaths.length != requestPaths.length)
            return false;
        for (int i = 0; i < methodPaths.length; i++) {
            if (!methodPaths[i].equals(requestPaths[i]) && !methodPaths[i].startsWith(":"))
                return false;
        }
        return true;
    }

    public static String removeFirstPath(String splitter, String[] keys) {        
        if (keys == null || keys.length <= 1)
            return "";
    
        String[] newArray = Arrays.copyOfRange(keys, 1, keys.length);
        return String.join(splitter, newArray);
    }

    public static String[] regexPath(String splitter, String path) {
        StringBuilder pathCorrector = new StringBuilder(path);
        System.out.println(Arrays.toString(splitPath(splitter, path)));
        if (path.length() > 0 && splitPath(splitter, path).length <= 1 && path.charAt(path.length() - 1) != ('/'))
            pathCorrector.append("/");
        String finalPath = pathCorrector.toString();
        System.out.println(finalPath);
        Pattern pattern = Pattern.compile(splitter);
        Matcher matcher = pattern.matcher(finalPath);

        List<String> paths = new ArrayList<>();
        while (matcher.find())
            paths.add(matcher.group(0));
        return paths.toArray(new String[0]);
    }
}