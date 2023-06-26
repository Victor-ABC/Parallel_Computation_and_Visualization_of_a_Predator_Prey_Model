package main.simulation.analysis;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import main.simulation.config.Config;

public class Util {
    public static List<String> getValuesOfConfigProperties(Config config, long estimatedTime) {
        List<String> result = new ArrayList<>();
        Class<?> myClass = config.getClass();
        result.add(convertToString(estimatedTime));//Always: Time first
        for (String header : config.getMetrics().useFields) {
            String methodName = "get" + uppercaseFirstChar(header);
            try {
                Method method = myClass.getMethod(methodName);//Reflection
                String value = convertToString(method.invoke(config));
                result.add(value);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String uppercaseFirstChar(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        char firstChar = Character.toUpperCase(input.charAt(0));
        String remainingChars = input.substring(1);

        return firstChar + remainingChars;
    }

    public static String convertToString(Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        } else {
            return String.valueOf(obj);
        }
    }

    static String prittyFormatDate(long timeMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("_dd_MM_yy");
        return dateFormat.format(new Date(timeMillis));
    }
}
