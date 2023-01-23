package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.exception.EntityParserException;
import ua.maksym.hlushchenko.util.ReflectionUtil;
import ua.maksym.hlushchenko.util.StringUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ua.maksym.hlushchenko.dao.db.sql.EntityParser.*;
import static ua.maksym.hlushchenko.dao.db.sql.QueryBuilder.*;

public class GenericQueryCreator {
    public static String generateQueryByMethodName(Class<?> entityClass, String name) {
        if (name.matches("find(All|By(((?:(?!Or|And|Greater|Lower)[A-Z][a-z0-9]+)+)(Lower|Greater)?)" +
                "((And|Or)(((?:(?!Or|And|Greater|Lower)[A-Z][a-z0-9]+)+)(Lower|Greater)?))*|((?:[A-z][a-z0-9]+)+s))?")) {
            return generateFind(entityClass, name);
        } else if (name.equals("save")) {
            return generateSave(entityClass);
        } else if (name.matches("update((By([A-Z]\\w*)+)(And([A-Z]\\w*)+)*)?")) {
            return generateUpdate(entityClass, name);
        } else if (name.matches("delete((By([A-Z]\\w*)+)(And([A-Z]\\w*)+)*)?")) {
            return generateDelete(entityClass, name);
        } else {
            throw new IllegalArgumentException("Can't parse operation name");
        }
    }

    private static String generateFind(Class<?> entityClass, String name) {
        QueryBuilder queryBuilder = QueryRelationUtil.generateSelectQueryBuilder(entityClass);

        if (name.equals("find")) {
            queryBuilder.addCondition(getTableNameOf(entityClass),
                    getIdColumnNameOf(entityClass),
                    QueryBuilder.ConditionOperator.EQUALS);
        } else if (name.startsWith("findBy")) {
            String conditions = name.substring(name.indexOf("By") + "By".length());
            parseConditions(entityClass, queryBuilder, conditions);
        } else if (name.matches("find((?:[A-z][a-z0-9]+)+s)")) {
            String collectionField = name.substring(name.indexOf("find") + "find".length());


        }

        return queryBuilder.toString();
    }

    private static void parseConditions(Class<?> entityClass, QueryBuilder queryBuilder, String conditions) {
        List<Class<?>> hierarchy = getEntityHierarchyOf(entityClass);
        Pattern pattern = Pattern.compile("(Or|And|Greater|Lower)|((?!Or|And|Greater|Lower)[A-Z][a-z0-9]+)+");
        Matcher matcher = pattern.matcher(conditions);

        QueryBuilder.ConditionOperator operator = QueryBuilder.ConditionOperator.EQUALS;
        while (matcher.find()) {
            String word = matcher.group();

            if (word.equals("Lower") | word.equals("Greater")) {
                operator = QueryBuilder.ConditionOperator.valueOf(word.toUpperCase());
            } else if (word.equals("And") | word.equals("Or")) {
                queryBuilder.addConditionConcatenation(QueryBuilder.ConditionOperator.valueOf(word.toUpperCase()));
            } else {
                String fieldName = StringUtil.toLowerCapCase(word);
                try {
                    Field field = null;
                    for (int i = hierarchy.size() - 1; i >= 0; i--) {
                        entityClass = hierarchy.get(i);

                        if (ReflectionUtil.isContainsDeclaredField(entityClass, fieldName)) {
                            field = entityClass.getDeclaredField(fieldName);
                        }
                    }

                    if (field == null) {
                        new EntityParserException("Any subclass of entity " +
                                "doesn't contain specified field" + fieldName);
                    }

                    String tableName = getTableNameOf(entityClass);
                    String column = getColumnNameOf(field);
                    queryBuilder.addCondition(tableName, column, operator);

                    operator = ConditionOperator.EQUALS;
                } catch (NoSuchFieldException e) {
                    throw new EntityParserException(e);
                }
            }
        }
    }

    private static String generateSave(Class<?> entityClass) {
        return new QueryBuilder(QueryBuilder.QueryType.INSERT).
                setTable(getTableNameOf(entityClass)).
                addAllColumns(getTableNameOf(entityClass), getColumnNamesOf(entityClass)).
                toString();
    }

    private static String generateUpdate(Class<?> entityClass, String name) {
        QueryBuilder queryBuilder = new QueryBuilder(QueryBuilder.QueryType.UPDATE).
                setTable(getTableNameOf(entityClass)).
                addAllColumns(getTableNameOf(entityClass), getColumnNamesOf(entityClass));

        if (name.equals("update")) {
            queryBuilder.addCondition(getTableNameOf(entityClass),
                    getIdColumnNameOf(entityClass),
                    QueryBuilder.ConditionOperator.EQUALS);
        } else if (name.startsWith("updateBy")) {
            String conditions = name.substring(name.indexOf("updateBy") + "updateBy".length());
            parseConditions(entityClass, queryBuilder, conditions);
        }

        return queryBuilder.toString();
    }

    private static String generateDelete(Class<?> entityClass, String name) {
        QueryBuilder queryBuilder = new QueryBuilder(QueryBuilder.QueryType.DELETE).
                setTable(getTableNameOf(entityClass));

        if (name.equals("delete")) {
            queryBuilder.addCondition(getTableNameOf(entityClass),
                    getIdColumnNameOf(entityClass),
                    QueryBuilder.ConditionOperator.EQUALS);
        } else if (name.startsWith("deleteBy")) {
            String conditions = name.substring(name.indexOf("deleteBy") + "deleteBy".length());
            parseConditions(entityClass, queryBuilder, conditions);
        }

        return queryBuilder.toString();
    }

    public static String createInsertQuery(Class<?> entityClass, Object entity) {
        String tableName = getTableNameOf(entityClass);
        List<String> columns = getColumnNamesOf(entityClass);
        List<Object> values = getColumnValuesOf(entityClass, entity);
        return new QueryBuilder(QueryType.INSERT).
                setTable(tableName).
                addAllColumns(tableName, columns).
                addAllValues(values).
                toString();
    }

    public static String createUpdateQuery(Class<?> entityClass, Object entity) {
        String tableName = getTableNameOf(entityClass);
        List<String> columns = getColumnNamesOf(entityClass);
        List<Object> values = getColumnValuesOf(entityClass, entity);
        String idColumn = getIdColumnNameOf(entityClass);
        Object idValue = getIdValue(entityClass, entityClass);
        return new QueryBuilder(QueryType.UPDATE).
                setTable(tableName).
                addAllColumns(tableName, columns).
                addAllValues(values).
                addCondition(tableName, idColumn, ConditionOperator.EQUALS, idValue).
                toString();
    }

    public static String createDeleteQuery(Class<?> entityClass, Object idValue) {
        String tableName = getTableNameOf(entityClass);
        String idColumn = getIdColumnNameOf(entityClass);
        return  new QueryBuilder(QueryType.DELETE).
                setTable(tableName).
                addCondition(tableName, idColumn, ConditionOperator.EQUALS, idValue).
                toString();
    }
}
