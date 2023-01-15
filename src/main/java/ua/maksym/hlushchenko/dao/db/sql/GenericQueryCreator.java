package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.db.sql.annotations.OneToMany;
import ua.maksym.hlushchenko.exception.EntityParserException;
import ua.maksym.hlushchenko.util.ReflectionUtil;
import ua.maksym.hlushchenko.util.StringUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ua.maksym.hlushchenko.dao.db.sql.EntityParser.*;

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
        QueryBuilder queryBuilder = generateSelectQueryBuilder(entityClass);

        if (name.equals("find")) {
            queryBuilder.addCondition(getTableName(entityClass),
                    getIdColumnName(entityClass),
                    QueryBuilder.ConditionOperator.EQUALS);
        } else if (name.startsWith("findBy")) {
            String conditions = name.substring(name.indexOf("By") + "By".length());
            parseConditions(entityClass, queryBuilder, conditions);
        } else if (name.matches("find((?:[A-z][a-z0-9]+)+s)")) {
            String collectionField = name.substring(name.indexOf("find") + "find".length());


        }

        return queryBuilder.toString();
    }

    private static QueryBuilder generateSelectQueryBuilder(Class<?> entityClass) {
        QueryBuilder queryBuilder = new QueryBuilder(QueryBuilder.QueryType.SELECT).
                setTable(getTableName(entityClass)).
                addAllColumns(getTableName(entityClass), getColumnNames(entityClass));

        List<Class<?>> superTypes = getEntitiesHierarchyOf(entityClass);
        for (int i = 1; i < superTypes.size(); i++) {
            Class<?> prevType = superTypes.get(i - 1);
            Class<?> currType = superTypes.get(i);
            queryBuilder.addJoin(getTableName(prevType), getTableName(currType),
                    getIdColumnName(prevType), getIdColumnName(currType)).
                    addAllColumns(getTableName(currType), getColumnNames(currType));
        }

        return queryBuilder;
    }

    private static void parseConditions(Class<?> entityClass, QueryBuilder queryBuilder, String conditions) {
        List<Class<?>> subTypes = getEntitiesHierarchyOf(entityClass);
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
                Class<?> containedClass = subTypes.stream().
                        filter(clazz -> ReflectionUtil.isContainsDeclaredField(clazz, fieldName)).
                        findFirst().
                        orElseThrow(() -> new EntityParserException(
                                "Any subclass of entity doesn't contain specified field" + fieldName));

                String tableName = getTableName(containedClass);
                try {
                    Field field = containedClass.getDeclaredField(fieldName);
                    String column = getColumnNameFor(field);
                    queryBuilder.addCondition(tableName, column, operator);

                    operator = QueryBuilder.ConditionOperator.EQUALS;
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static String generateSave(Class<?> entityClass) {
        return new QueryBuilder(QueryBuilder.QueryType.INSERT).
                setTable(getTableName(entityClass)).
                addAllColumns(getTableName(entityClass), getColumnNames(entityClass)).
                toString();
    }

    private static String generateUpdate(Class<?> entityClass, String name) {
        QueryBuilder queryBuilder = new QueryBuilder(QueryBuilder.QueryType.UPDATE).
                setTable(getTableName(entityClass)).
                addAllColumns(getTableName(entityClass), getColumnNames(entityClass));

        if (name.equals("update")) {
            queryBuilder.addCondition(getTableName(entityClass),
                    getIdColumnName(entityClass),
                    QueryBuilder.ConditionOperator.EQUALS);
        } else if (name.startsWith("updateBy")) {
            String conditions = name.substring(name.indexOf("updateBy") + "updateBy".length());
            parseConditions(entityClass, queryBuilder, conditions);
        }

        return queryBuilder.toString();
    }

    private static String generateDelete(Class<?> entityClass, String name) {
        QueryBuilder queryBuilder = new QueryBuilder(QueryBuilder.QueryType.DELETE).
                setTable(getTableName(entityClass));

        if (name.equals("delete")) {
            queryBuilder.addCondition(getTableName(entityClass),
                    getIdColumnName(entityClass),
                    QueryBuilder.ConditionOperator.EQUALS);
        } else if (name.startsWith("deleteBy")) {
            String conditions = name.substring(name.indexOf("deleteBy") + "deleteBy".length());
            parseConditions(entityClass, queryBuilder, conditions);
        }

        return queryBuilder.toString();
    }
}
