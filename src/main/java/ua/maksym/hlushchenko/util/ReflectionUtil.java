package ua.maksym.hlushchenko.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class ReflectionUtil {
    public static List<Class<?>> getSuperClassesOf(Class<?> clazz) {
        List<Class<?>> subTypes = new ArrayList<>();

        Class<?> currClass = clazz;
        while (currClass != null) {
            subTypes.add(currClass);
            currClass = currClass.getSuperclass();
        }

        return subTypes;
    }

    public static boolean isContainsDeclaredField(Class<?> clazz, String fieldName) {
        return Arrays.stream(clazz.getDeclaredFields()).
                anyMatch(field -> field.getName().equals(fieldName));
    }

    public static List<Field> getDeclaredFieldsAnnotatedWith(Class<?> clazz,
                                                             Class<? extends Annotation> annotationClass) {
        return Arrays.stream(clazz.getDeclaredFields()).
                filter(field -> field.isAnnotationPresent(annotationClass)).
                collect(Collectors.toList());
    }

    public static List<Field> getDeclaredFieldsAnnotatedWithOneOf(Class<?> clazz,
                                                                  List<Class<? extends Annotation>> annotations) {
        return Arrays.stream(clazz.getDeclaredFields()).
                filter(field -> annotations.stream().
                        anyMatch(field::isAnnotationPresent)).
                collect(Collectors.toList());
    }
}
