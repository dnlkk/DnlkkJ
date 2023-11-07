package com.dnlkk.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityIgnoreUtils {
    public static String[] getIgnoredFieldFromMethod(Method method, Class<?> valueClass) {
        List<String> ignored = new ArrayList<>();

        String[] array = method.getName().split("Ignored");
        if (array.length > 1) {
            String[] ignoredFields = array[1].split("(?=[A-Z])");
            StringBuilder stringBuilder = new StringBuilder();

            for (String ignoredFieldTemp : ignoredFields) {
                String ignoredField = ignoredFieldTemp.toLowerCase().charAt(0) + ignoredFieldTemp.substring(1);

                if (ignoredField.equals("and")) {
                    stringBuilder = new StringBuilder();
                    continue;
                }

                if (!stringBuilder.isEmpty()) {
                    stringBuilder.append(ignoredFieldTemp);
                    ignoredField = stringBuilder.toString();
                }

                String finalIgnoredField = ignoredField;
                if (Arrays.stream(valueClass.getDeclaredFields()).filter(field -> field.getName().equals(finalIgnoredField)).toArray().length != 0) {
                    ignored.add(ignoredField);
                    stringBuilder = new StringBuilder();
                } else if (stringBuilder.isEmpty())
                    stringBuilder.append(ignoredFieldTemp);
            }
        }
        return ignored.toArray(new String[0]);
    }
}