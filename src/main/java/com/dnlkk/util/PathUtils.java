package com.dnlkk.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathUtils {
    public static String[] splitPath(String splitter, String path) {
        String[] keys = path.split(splitter);
        return keys;
    }

    public static String removeFirstPath(String[] keys, String path, int distance) {
        String keyFirst = keys[0];
        if (keys.length <= 1)
            return null;
        return path.substring(keyFirst.length() + distance);
    }
    public static String removeFirstPath(String[] keys, String path) {
        return removeFirstPath(keys, path, 1);
    }

    public static String[] regexPath(String splitter, String path) {
        Pattern pattern = Pattern.compile(splitter);
        Matcher matcher = pattern.matcher(path);

        List<String> paths = new ArrayList<>();
        while (matcher.find())
            paths.add(matcher.group(0));
        return paths.toArray(new String[0]);
    }
}