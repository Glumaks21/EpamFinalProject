package ua.maksym.hlushchenko.dao.db.sql;


import ua.maksym.hlushchenko.dao.db.sql.annotations.*;
import ua.maksym.hlushchenko.exception.EntityParserException;
import ua.maksym.hlushchenko.util.StringUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import static ua.maksym.hlushchenko.util.ReflectionUtil.*;

public class EntityParser {
    private static final List<Class<? extends Annotation>> columnAnnotations = List.of(
            Id.class, Column.class, JoinColumn.class);

    public static List<Class<?>> getEntitiesHierarchyOf(Class<?> entityClass) {
        return getSuperClassesOf(entityClass).stream().
                takeWhile(clazz -> clazz.isAnnotationPresent(Table.class)).
                collect(Collectors.toList());
    }

    public static String getTableName(Class<?> entityClass) {
        checkTableAnnotation(entityClass);
        return entityClass.getAnnotation(Table.class).value();
    }

    private static void checkTableAnnotation(Class<?> entityClass) {
        if (!entityClass.isAnnotationPresent(Table.class)) {
            throw new EntityParserException("Class " + entityClass +
                    " doesn't contain " + Table.class + " annotation");
        }
    }

    public static String getIdColumnName(Class<?> entityClass) {
        checkTableAnnotation(entityClass);
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field.getAnnotation(Id.class).value();
            }
        }

        throw new EntityParserException("Class " + entityClass +
                " doesn't contain " + Id.class + " annotation");
    }

    public static String getColumnNameFor(Field field) {
        checkTableAnnotation(field.getDeclaringClass());
        return extractColumnNameFromAnnotatedField(field);
    }

    public static String extractColumnNameFromAnnotatedField(Field field) {
        if (field.isAnnotationPresent(Id.class)) {
            return field.getAnnotation(Id.class).value();
        } else if (field.isAnnotationPresent(Column.class)) {
            return field.getAnnotation(Column.class).value();
        } else if (field.isAnnotationPresent(JoinColumn.class)) {
            return field.getAnnotation(JoinColumn.class).value();
        } else {
            throw new EntityParserException("Field " + field + " doesn't " +
                    "contain any of the column annotations");
        }
    }

    public static List<String> getColumnNames(Class<?> entityClass) {
        return getDeclaredFieldsAnnotatedWithOneOf(entityClass, columnAnnotations).stream().
                map(EntityParser::extractColumnNameFromAnnotatedField).
                collect(Collectors.toList());
    }

    public static List<Object> getColumnValues(Class<?> entityClass) {
        try {
            List<Object> values = new ArrayList<>();
            for (Field field : getDeclaredFieldsAnnotatedWithOneOf(entityClass, columnAnnotations)) {
                Method getter = getGetter(field);
                Object value = getter.invoke(field);
                values.add(value);
            }
            return values;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new EntityParserException(e);
        }
    }

    public static Method getGetter(Field field) {
        try {
            return field.getDeclaringClass().getMethod(
                    "get" + StringUtil.toInitCapCase(field.getName()));
        } catch (NoSuchMethodException e) {
            throw new EntityParserException("Getter wasn't found");
        }
    }

    public static Method getSetter(Field field) {
        try {
            return field.getDeclaringClass().getMethod(
                    "set" + StringUtil.toInitCapCase(field.getName()),
                    field.getType());
        } catch (NoSuchMethodException e) {
            throw new EntityParserException("Setter wasn't found");
        }
    }
}
