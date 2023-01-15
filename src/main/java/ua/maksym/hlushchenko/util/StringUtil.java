package ua.maksym.hlushchenko.util;

public class StringUtil {
    public static String toInitCapCase(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String toLowerCapCase(String str) {
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
