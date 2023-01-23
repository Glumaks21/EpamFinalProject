package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.exception.EntityParserException;
import ua.maksym.hlushchenko.orm.annotations.*;
import ua.maksym.hlushchenko.util.StringUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static ua.maksym.hlushchenko.util.ReflectionUtil.*;

public class EntityParser {
    private static final List<Class<? extends Annotation>> columnAnnotations = List.of(
            Id.class, Column.class, JoinColumn.class);

    public static List<Class<?>> getEntityHierarchyOf(Class<?> entityClass) {
        List<Class<?>> hierarchy = new LinkedList<>();

        List<Class<?>> ancestors = getAncestorListOf(entityClass);
        for (int i = ancestors.size() - 1; i >= 0; i--) {
            Class<?> ancestor = ancestors.get(i);

            if (!hierarchy.isEmpty() || ancestor.isAnnotationPresent(Table.class)) {
                hierarchy.add(ancestor);
            }
        }

        if (hierarchy.isEmpty()) {
            throw new EntityParserException("There are no class of entities hierarchy");
        }

        return hierarchy;
    }

    private static List<Class<?>> getEntitiesHierarchyByTableOf(Class<?> entityClass) {
        List<Class<?>> hierarchy = new LinkedList<>();

        Class<?> currClass = entityClass;
        while (currClass != null && !currClass.isAnnotationPresent(Table.class)) {
            hierarchy.add(0, currClass);
            currClass = currClass.getSuperclass();
        }

        if (currClass != null) {
            hierarchy.add(0, currClass);
        } else {
            throw new EntityParserException("There are no " + Table.class + " in class hierarchy");
        }

        return  hierarchy;
    }

    public static String getTableNameOf(Class<?> entityClass) {
        List<Class<?>> hierarchy = getEntitiesHierarchyByTableOf(entityClass);
        Class<?> tableClass = hierarchy.get(0);
        return tableClass.getAnnotation(Table.class).value();
    }


    public static String getIdColumnNameOf(Class<?> entityClass) {
        List<Class<?>> hierarchy = getEntitiesHierarchyByTableOf(entityClass);
        Class<?> tableClass = hierarchy.get(0);

        Class<?> ancestor = tableClass.getSuperclass();
        if (isEntityClass(ancestor)) {
            if (tableClass.isAnnotationPresent(IdMappedByColumn.class)) {
                return tableClass.getAnnotation(IdMappedByColumn.class).value();
            }
            return getIdColumnNameOf(ancestor);
        }

        for (Field field : tableClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field.getAnnotation(Id.class).value();
            }
        }

        throw new EntityParserException("Class " + entityClass + " doesn't contain id column");
    }

    private static boolean isEntityClass(Class<?> entityClass) {
        while (entityClass != null && !entityClass.isAnnotationPresent(Table.class)) {
            entityClass = entityClass.getSuperclass();
        }

        return entityClass != null;
    }

    public static String getColumnNameOf(Field field) {
        if (field.isAnnotationPresent(Id.class)) {
            return field.getAnnotation(Id.class).value();
        } else if (field.isAnnotationPresent(Column.class)) {
            return field.getAnnotation(Column.class).value();
        } else if (field.isAnnotationPresent(JoinColumn.class)) {
            return field.getAnnotation(JoinColumn.class).value();
        } else {
            throw new EntityParserException("Field " + field + " is not table column");
        }
    }

    public static List<String> getColumnNamesOf(Class<?> entityClass) {
        List<String> columnNames = new LinkedList<>();
        List<Class<?>> hierarchy = getEntitiesHierarchyByTableOf(entityClass);

        Class<?> tableClass = hierarchy.get(0);
        String idColumn = null;
        if (isEntityClass(tableClass.getSuperclass())) {
            if (tableClass.isAnnotationPresent(IdMappedByColumn.class)) {
                idColumn = tableClass.getAnnotation(IdMappedByColumn.class).value();
            } else {
                idColumn = getIdColumnNameOf(tableClass.getSuperclass());
            }
        }

        for (Class<?> ancestor : hierarchy) {
            for (Field field : ancestor.getDeclaredFields()) {
                if (isColumn(field)) {
                    if (field.isAnnotationPresent(Id.class) && idColumn != null) {
                        columnNames.remove(idColumn);
                        idColumn = null;
                    } else {
                        columnNames.add(getColumnNameOf(field));
                    }
                }
            }
        }

        if (idColumn != null) {
            columnNames.add(0, idColumn);
        }

        return columnNames;
    }

    private static boolean isColumn(Field field) {
        return columnAnnotations.stream().
                anyMatch(field::isAnnotationPresent);
    }

    public static Object getIdValue(Class<?> entityClass, Object entity) {
        List<Class<?>> hierarchy = getEntityHierarchyOf(entityClass);
        Class<?> tableClass = hierarchy.get(0);

        for (Field field : tableClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return getValueOf(field, entity);
            }
        }

        throw new EntityParserException("Class " + entityClass + " doesn't contain id column");
    }

    public static List<Object> getColumnValuesOf(Class<?> entityClass, Object entity) {
        List<Object> values = new ArrayList<>();
        List<Class<?>> hierarchy = getEntitiesHierarchyByTableOf(entityClass);

        for (Class<?> ancestor : hierarchy) {
            for (Field field : ancestor.getDeclaredFields()) {
                if (isColumn(field)) {
                    Object value = getValueOf(field, entity);

                    if (value != null && field.isAnnotationPresent(JoinColumn.class)) {
                        value = getIdValue(field.getType(), value);
                    }

                    values.add(value);
                }
            }
        }

        return values;
    }

    public static Object getValueOf(Field field, Object entity) {
        try {
            String getterName = "get" + StringUtil.toInitCapCase(field.getName());
            Method getter = field.getDeclaringClass().getMethod(getterName);
            return getter.invoke(entity);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new EntityParserException(e);
        } catch (NoSuchMethodException e) {
            throw new EntityParserException("Getter wasn't found");
        }
    }

    public static void setValueTo(Field field, Object value, Object entity) {
        try {
            String getterName = "set" + StringUtil.toInitCapCase(field.getName());
            Method setter = field.getDeclaringClass().getMethod(getterName, field.getType());
            setter.invoke(entity, value);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new EntityParserException(e);
        } catch (NoSuchMethodException e) {
            throw new EntityParserException("Setter wasn't found");
        }
    }
}
