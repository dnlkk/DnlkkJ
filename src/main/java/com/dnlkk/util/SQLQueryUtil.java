package com.dnlkk.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLQueryUtil {
    public static String[] getParamsFromQuery(String sql) {
        List<String> params = new ArrayList<>();
        
        Pattern pattern = Pattern.compile("(:[a-zA-Z]*)");
        Matcher matcher = pattern.matcher(sql);

        while (matcher.find())
            params.add(matcher.group().substring(1));

        return params.toArray(new String[0]);
    }

    public static String removeParamsFromQuery(String sql, String[] params) {
        String sqlWithinParams = sql;
        for (String param : params) {
            sqlWithinParams = sqlWithinParams.replace(":" + param, "?");
        }
        return sqlWithinParams;
    }
}