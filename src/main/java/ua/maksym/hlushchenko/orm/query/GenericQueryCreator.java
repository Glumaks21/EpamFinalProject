package ua.maksym.hlushchenko.orm.query;

import java.util.*;

import static ua.maksym.hlushchenko.orm.entity.EntityParser.*;

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
        SelectQueryBuilder builder = QueryRelationUtil.generateSelectQueryBuilder(entityClass);

        if (name.equals("find")) {
            builder.addCondition(getTableNameOf(entityClass),
                    getIdColumnNameOf(entityClass),
                    ConditionOperator.EQUALS);
        } else if (name.startsWith("findBy")) {
            String conditions = name.substring(name.indexOf("By") + "By".length());
            // parseConditions(entityClass, builder, conditions);
        } else if (name.matches("find((?:[A-z][a-z0-9]+)+s)")) {
            String collectionField = name.substring(name.indexOf("find") + "find".length());


        }

        return builder.toString();
    }

//    private static void parseConditions(Class<?> entityClass,
//                                        QueryBuilderWithConditions<?> queryBuilder, String conditions) {
//        List<Class<?>> hierarchy = getEntityHierarchyOf(entityClass);
//        Pattern pattern = Pattern.compile("(Or|And|Greater|Lower)|((?!Or|And|Greater|Lower)[A-Z][a-z0-9]+)+");
//        Matcher matcher = pattern.matcher(conditions);
//
//        ConditionOperator operator = ConditionOperator.EQUALS;
//        while (matcher.find()) {
//            String word = matcher.group();
//
//            if (word.equals("Lower") | word.equals("Greater")) {
//                operator = ConditionOperator.valueOf(word.toUpperCase());
//            } else if (word.equals("And") | word.equals("Or")) {
//                queryBuilder.addConditionConcatenation(ConditionOperator.valueOf(word.toUpperCase()));
//            } else {
//                String fieldName = StringUtil.toLowerCapCase(word);
//                try {
//                    Field field = null;
//                    for (int i = hierarchy.size() - 1; i >= 0; i--) {
//                        entityClass = hierarchy.get(i);
//
//                        if (ReflectionUtil.isContainsDeclaredField(entityClass, fieldName)) {
//                            field = entityClass.getDeclaredField(fieldName);
//                        }
//                    }
//
//                    if (field == null) {
//                        new EntityParserException("Any subclass of entity " +
//                                "doesn't contain specified field" + fieldName);
//                    }
//
//                    String tableName = getTableNameOf(entityClass);
//                    String column = getColumnNameOf(field);
//                    queryBuilder.addCondition(tableName, column, operator);
//
//                    operator = ConditionOperator.EQUALS;
//                } catch (NoSuchFieldException e) {
//                    throw new EntityParserException(e);
//                }
//            }
//        }
//    }


    private static String generateSave(Class<?> entityClass) {
        return new InsertQueryBuilder().
                addMainTable(getTableNameOf(entityClass)).
                addAllColumns(getTableNameOf(entityClass), getColumnNamesOf(entityClass)).
                toString();
    }

    private static String generateUpdate(Class<?> entityClass, String name) {
        UpdateQueryBuilder builder = new UpdateQueryBuilder().
                addMainTable(getTableNameOf(entityClass)).
                addAllColumns(getTableNameOf(entityClass), getColumnNamesOf(entityClass));

        if (name.equals("update")) {
            builder.addCondition(getTableNameOf(entityClass),
                    getIdColumnNameOf(entityClass),
                    ConditionOperator.EQUALS);
        } else if (name.startsWith("updateBy")) {
            String conditions = name.substring(name.indexOf("updateBy") + "updateBy".length());
            //parseConditions(entityClass, builder, conditions);
        }

        return builder.toString();
    }

    private static String generateDelete(Class<?> entityClass, String name) {
        DeleteQueryBuilder queryBuilder = new DeleteQueryBuilder().
                addMainTable(getTableNameOf(entityClass));

        if (name.equals("delete")) {
            queryBuilder.addCondition(getTableNameOf(entityClass),
                    getIdColumnNameOf(entityClass),
                    ConditionOperator.EQUALS);
        } else if (name.startsWith("deleteBy")) {
            String conditions = name.substring(name.indexOf("deleteBy") + "deleteBy".length());
            //parseConditions(entityClass, queryBuilder, conditions);
        }

        return queryBuilder.toString();
    }

    public static String createSelectAll(Class<?> entityClass) {
        return QueryRelationUtil.generateSelectQueryBuilder(entityClass).toString();
    }

    public static String createSelectById(Class<?> entityClass) {
        return QueryRelationUtil.generateSelectQueryBuilder(entityClass).
                addCondition(getTableNameOf(entityClass), getIdColumnNameOf(entityClass),
                        ConditionOperator.EQUALS).toString();
    }

    public static String createInsertQueryOf(Class<?> entityClass) {
        String tableName = getTableNameOf(entityClass);
        List<String> columns = getColumnNamesOf(entityClass);
        return new InsertQueryBuilder().
                addMainTable(tableName).
                addAllColumns(tableName, columns).
                toString();
    }


    public static String createUpdateByIdOf(Class<?> entityClass) {
        String tableName = getTableNameOf(entityClass);
        List<String> columns = getColumnNamesOf(entityClass);
        String idColumn = getIdColumnNameOf(entityClass);
        return new UpdateQueryBuilder().
                addMainTable(tableName).

                addAllColumns(tableName, columns).
                addCondition(tableName, idColumn, ConditionOperator.EQUALS).
                toString();
    }

    public static String createDeleteByIdQueryOf(Class<?> entityClass) {
        String tableName = getTableNameOf(entityClass);
        String idColumn = getIdColumnNameOf(entityClass);
        return new DeleteQueryBuilder().
                addMainTable(tableName).
                addCondition(tableName, idColumn, ConditionOperator.EQUALS).
                toString();
    }
}
