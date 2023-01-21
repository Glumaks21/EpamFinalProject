package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.exception.EntityParserException;
import ua.maksym.hlushchenko.orm.annotations.*;
import ua.maksym.hlushchenko.util.StringUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import static ua.maksym.hlushchenko.util.ReflectionUtil.*;

public class EntityUtil {
    private static final List<Class<? extends Annotation>> columnAnnotations = List.of(
            Id.class, Column.class, JoinColumn.class);

    public static List<Class<?>> getEntitiesHierarchyOf(Class<?> entityClass) {
        return getClassesHierarchyOf(entityClass).stream().
                takeWhile(clazz -> clazz.isAnnotationPresent(Table.class)).
                collect(Collectors.toList());
    }

    public static LinkedHashMap<String, List<String>> getTableAssociatedColumnsOf(Class<?> entityClass) {
        LinkedHashMap<String, List<String>> map = new LinkedHashMap<>();
        List<Class<?>> hierarchy = getEntitiesHierarchyOf(entityClass);
        for (Class<?> entity : hierarchy) {
            map.put(getTableName(entity), getColumnNames(entity));
        }
        return map;
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

    public static boolean isColumn(Field field) {
        return columnAnnotations.stream().
                anyMatch(field::isAnnotationPresent);
    }

    public static String getIdColumnName(Class<?> entityClass) {
        checkTableAnnotation(entityClass);
        return Arrays.stream(entityClass.getDeclaredFields()).
                filter(field -> field.isAnnotationPresent(Id.class)).
                map(field -> field.getAnnotation(Id.class).value()).
                findFirst().orElseThrow(() -> new EntityParserException(
                        "Class " + entityClass + " doesn't contain " + Id.class));
    }

    public static String getColumnNameFor(Field field) {
        checkTableAnnotation(field.getDeclaringClass());
        return extractColumnNameFrom(field);
    }

    private static String extractColumnNameFrom(Field field) {
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
        return Arrays.stream(entityClass.getDeclaredFields()).
                filter(EntityUtil::isColumn).
                map(EntityUtil::extractColumnNameFrom).
                collect(Collectors.toList());
    }

    public static Object getIdValue(Object entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields()).
                filter(field -> field.isAnnotationPresent(Id.class)).
                map(field -> getValueOf(field, entity)).
                findFirst().orElseThrow(() -> new EntityParserException(
                        "Entity doesn't contain field with " + Id.class));
    }

    public static List<Object> getColumnValuesOf(Object entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields()).
                filter(EntityUtil::isColumn).
                map(field -> getValueOf(field, entity)).
                collect(Collectors.toList());
    }

    public static Object getValueOf(Field field, Object entity) {
        try {
            Method getter = getGetter(field);
            return getter.invoke(entity);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new EntityParserException(e);
        }
    }

    private static Method getGetter(Field field) {
        try {
            String getterName = "get" + StringUtil.toInitCapCase(field.getName());
            return field.getDeclaringClass().getMethod(getterName);
        } catch (NoSuchMethodException e) {
            throw new EntityParserException("Getter wasn't found");
        }
    }

    public static void setValueTo(Field field, Object value, Object entity) {
        try {
            Method setter = getSetter(field);
            setter.invoke(entity, value);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new EntityParserException(e);
        }
    }

    private static Method getSetter(Field field) {
        try {
            String getterName = "set" + StringUtil.toInitCapCase(field.getName());
            return field.getDeclaringClass().getMethod(getterName, field.getType());
        } catch (NoSuchMethodException e) {
            throw new EntityParserException("Setter wasn't found");
        }
    }
}
