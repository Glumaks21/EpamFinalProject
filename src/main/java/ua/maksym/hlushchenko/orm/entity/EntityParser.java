package ua.maksym.hlushchenko.orm.entity;

import ua.maksym.hlushchenko.orm.exception.EntityParserException;
import ua.maksym.hlushchenko.orm.entity.annotations.*;
import ua.maksym.hlushchenko.util.ReflectionUtil;
import ua.maksym.hlushchenko.util.StringUtil;

import java.lang.reflect.*;
import java.util.*;

public class EntityParser {
    public static boolean isEntity(Object entity) {
        return entity != null && entity.getClass().isAnnotationPresent(Entity.class);
    }

    public static boolean isTableClass(Class<?> aClass) {
        return aClass != null && aClass.isAnnotationPresent(Table.class);
    }

    public static boolean isColumnField(Field field) {
        return field != null && (field.isAnnotationPresent(Id.class) ||
                field.isAnnotationPresent(Column.class) ||
                field.isAnnotationPresent(JoinColumn.class));
    }

    public static boolean isSingleRelatedField(Field field) {
        return field != null &&
                (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class) );
    }

    public static boolean isCollectionRelatedField(Field field) {
        return field != null &&
                (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class) );
    }

    public static List<Class<?>> getFullClassHierarchyOf(Class<?> aClass) {
        if (aClass == null || aClass.getSuperclass() == null) {
            throw new IllegalArgumentException("Class: " + aClass);
        }

        LinkedList<Class<?>> hierarchy = new LinkedList<>();

        Class<?> currClass = aClass;
        while (currClass != Object.class) {
            hierarchy.addFirst(validateClass(currClass));
            currClass = currClass.getSuperclass();
        }

        if (!hierarchy.getFirst().isAnnotationPresent(Table.class)) {
            throw new EntityParserException("Hierarchy is incorrect, " +
                    "first class must be annotated with: " + Table.class);
        }

        return hierarchy;
    }

    private static Class<?> validateClass(Class<?> aClass) {
        if (isTableClass(aClass)) {
            return validateTableClass(aClass);
        }
        return validateSimpleClass(aClass);
    }

    private static Class<?> validateTableClass(Class<?> tableClass) {
        List<Field> idFields = ReflectionUtil.getDeclaredFieldsAnnotatedWith(tableClass, Id.class);

        if (idFields.size() > 1) {
            throw new EntityParserException("Class " + tableClass + " contains more then two Id annotations");
        } else if (tableClass.getSuperclass() == Object.class && idFields.isEmpty()) {
            throw new EntityParserException("Class " + tableClass + " doesn't contain Id annotation");
        } else if (tableClass.getSuperclass() != Object.class && !idFields.isEmpty()) {
            throw new EntityParserException("Id annotation in joined table class, strategy join column");
        }

        return tableClass;
    }

    private static Class<?> validateSimpleClass(Class<?> aClass) {
        List<Field> idFields = ReflectionUtil.getDeclaredFieldsAnnotatedWith(aClass, Id.class);
        List<Field> columnFields = ReflectionUtil.getDeclaredFieldsAnnotatedWithOneOf(
                aClass, List.of(Id.class, Column.class, JoinColumn.class));

        if (!idFields.isEmpty()) {
            throw new EntityParserException("Class " + aClass + " contains Id annotation without Table annotation");
        } else if (columnFields.isEmpty()) {
            throw new EntityParserException("Class " + aClass + " doesn't contain any column annotation");
        }

        return aClass;
    }

    public static List<Class<?>> getClassHierarchyByTableOf(Class<?> aClass) {
        LinkedList<Class<?>> tableClasses = new LinkedList<>();

        List<Class<?>> fullHierarchy = getFullClassHierarchyOf(aClass);
        for (int i = fullHierarchy.size() - 1; i >= 0; i--) {
            Class<?> currClass = fullHierarchy.get(i);

            tableClasses.addFirst(currClass);
            if (isTableClass(currClass)) {
                return tableClasses;
            }
        }

        throw new EntityParserException("Impossible exception");
    }

    public static String getTableNameOf(Class<?> aClass) {
        return getClassHierarchyByTableOf(aClass).get(0).
                getAnnotation(Table.class).value();
    }

    public static String getIdColumnNameOf(Class<?> aClass) {
        List<Class<?>> hierarchy = getFullClassHierarchyOf(aClass);

        String idColumn = ReflectionUtil.getDeclaredFieldsAnnotatedWith(hierarchy.get(0), Id.class).
                get(0).getAnnotation(Id.class).value();

        for (int i = 1; i < hierarchy.size(); i++) {
            Class<?> currClass = hierarchy.get(i);

            if (isTableClass(currClass) && currClass.isAnnotationPresent(IdMappedByColumn.class)) {
                idColumn = currClass.getAnnotation(IdMappedByColumn.class).value();
            }
        }

        return idColumn;
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

    public static List<String> getColumnNamesOf(Class<?> aClass) {
        LinkedList<String> columnNames = new LinkedList<>();
        List<Class<?>> hierarchy = getClassHierarchyByTableOf(aClass);

        Class<?> tableClass = hierarchy.get(0);
        if (tableClass.getSuperclass() != Object.class) {
            columnNames.addFirst(getIdColumnNameOf(aClass));
        }

        for (Class<?> currClass : hierarchy) {
            for (Field field : currClass.getDeclaredFields()) {
                if (isColumnField(field)) {
                    String columnName = getColumnNameOf(field);
                    columnNames.add(columnName);
                }
            }
        }

        return columnNames;
    }

    public static Object getIdValueOf(Class<?> entityClass, Object entity) {
        List<Class<?>> tableClasses = getFullClassHierarchyOf(entityClass);
        Class<?> tableClass = tableClasses.get(0);

        for (Field field : tableClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return getValueOf(field, entity);
            }
        }

        throw new EntityParserException("Impossible exception");
    }

    public static List<Object> getColumnValuesOf(Class<?> entityClass, Object entity) {
        List<Object> values = new ArrayList<>();
        List<Class<?>> hierarchy = getClassHierarchyByTableOf(entityClass);

        Class<?> tableClass = hierarchy.get(0);
        if (tableClass.getSuperclass() != Object.class) {
            values.add(getIdValueOf(tableClass, entity));
        }

        for (Class<?> curClass : hierarchy) {
            for (Field field : curClass.getDeclaredFields()) {
                if (isColumnField(field)) {
                    Object value = getValueOf(field, entity);

                    if (value != null && field.isAnnotationPresent(JoinColumn.class)) {
                        value = getIdValueOf(field.getType(), value);
                    }

                    values.add(value);
                }
            }
        }

        return values;
    }

    public static Object getValueOf(Field field, Object entity) {
        try {
            String getterName;
            if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                getterName = "is" + StringUtil.toInitCapCase(field.getName());
            } else {
                getterName = "get" + StringUtil.toInitCapCase(field.getName());
            }

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

