package ua.maksym.hlushchenko.dao.db.sql;

import java.util.*;

public class QueryBuilder {
    private final QueryType queryType;

    private final List<String> tables;
    private final List<String> columns;
    private final List<String> values;

    private final List<String> joins;
    private final List<String> conditions;
    private final List<ConditionOperator> concatOperators;

    public QueryBuilder(QueryType queryType) {
        this.queryType = queryType;
        tables = new ArrayList<>();
        columns = new ArrayList<>();
        values = new ArrayList<>();
        joins = new ArrayList<>();
        conditions = new ArrayList<>();
        concatOperators = new ArrayList<>();
    }

    public QueryBuilder setTable(String table) {
        if (table == null || table.isBlank()) {
            throw new IllegalArgumentException();
        }
        if (!tables.isEmpty()) {
            throw new IllegalArgumentException("Table is already set: " + tables.get(0));
        }

        tables.add(table);
        return this;
    }

    public QueryBuilder addColumn(String table, String column) {
        if (!tables.contains(table) || column == null || column.isBlank()) {
            throw new IllegalArgumentException();
        }

        if (queryType == QueryType.SELECT) {
            column = convertToTableColumn(table, column) +
                    " AS " +
                    convertToResultSetColumn(table, column);
        }
        columns.add(column);
        return this;
    }

    public QueryBuilder addAllColumns(String table, List<String> columns) {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException();
        }
        columns.forEach(column -> addColumn(table, column));
        return this;
    }

    public QueryBuilder addValue(Object value) {
        if (queryType == QueryType.DELETE || queryType == QueryType.SELECT) {
            throw new IllegalStateException("Query type is " + queryType);
        }

        values.add(convertToValue(value));
        return this;
    }

    private String convertToValue(Object object) {
        if (object instanceof String) {
            return "'" + object + "'";
        }
        return object.toString();
    }

    public QueryBuilder addAllValues(List<?> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException();
        }
        values.forEach(this::addValue);
        return this;
    }

    public QueryBuilder addJoin(String targetTable, String joinedTable,
                                String targetColumn, String joinedColumn) {
        if (!tables.contains(targetTable)) {
            throw new IllegalArgumentException("Target table is unknown");
        } else if (queryType != QueryType.SELECT) {
            throw new IllegalStateException("Query type is " + queryType);
        }

        tables.add(joinedTable);
        String join = String.format("JOIN %s ON %s = %s",
                joinedTable,
                convertToTableColumn(targetTable, targetColumn),
                convertToTableColumn(joinedTable, joinedColumn));
        joins.add(join);
        return this;
    }

    public static String convertToTableColumn(String tableName, String columnName) {
        return tableName + "." + columnName;
    }

    public static String convertToResultSetColumn(String tableName, String columnName) {
        return "_" + tableName + "_" + columnName;
    }

    public QueryBuilder addCondition(String table, String column, ConditionOperator conditionOperator, Object value) {
        if (!tables.contains(table)) {
            throw new IllegalArgumentException("Table is unknown");
        } else if (queryType == QueryType.INSERT) {
            throw new IllegalStateException("Query type is " + queryType);
        } else if (conditions.size() - concatOperators.size() == 1) {
            throw new IllegalStateException("Add concatenation operator");
        }

        String condExp = convertToConditionExpression(
                convertToTableColumn(table, column),
                conditionOperator,
                value);
        conditions.add(condExp);
        return this;
    }

    public QueryBuilder addCondition(String table, String column, ConditionOperator conditionOperator) {
        return addCondition(table, column, conditionOperator, null);
    }

    private String convertToConditionExpression(String var, ConditionOperator conditionOperator, Object value) {
        return var.trim() + " " + conditionOperator.value + " " + (value == null ? "?" : convertToValue(value));
    }

    public void addConditionConcatenation(ConditionOperator conditionOperator) {
        if (concatOperators.size() == conditions.size()) {
            throw new IllegalStateException("Number of concatenation operators must be lower then conditions");
        }
        concatOperators.add(conditionOperator);
    }

    @Override
    public String toString() {
        switch (queryType) {
            case SELECT:
                return toSelectQueryString();
            case INSERT:
                return toInsertQueryString();
            case UPDATE:
                return toUpdateQueryString();
            case DELETE:
                return toDeleteQueryString();
            default:
                throw new EnumConstantNotPresentException(queryType.getClass(), queryType.name());
        }
    }

    private String toSelectQueryString() {
        return String.format("SELECT %s FROM %s%s%s",
                String.join(", ", columns),
                tables.get(0),
                toJoinsString(),
                toConditionsString()
        );
    }

    private String toJoinsString() {
        if (!joins.isEmpty()) {
            StringJoiner stringJoiner = new StringJoiner(" ", " ", "");
            joins.forEach(stringJoiner::add);
            return stringJoiner.toString();
        }
        return "";
    }

    private String toConditionsString() {
        if (conditions.isEmpty()) {
            return "";
        }

        StringBuilder strb = new StringBuilder(" WHERE ").
                append(conditions.get(0));
        if (conditions.size() > 1) {
            for (int i = 1; i < conditions.size(); i++) {
                strb.append(" ").
                        append(concatOperators.get(i - 1)).
                        append(" ").
                        append(conditions.get(i));
            }
        }

        return strb.toString();
    }

    private String toInsertQueryString() {
        return String.format("INSERT INTO %s(%s) VALUES(%s)",
                tables.get(0),
                String.join(", ", columns),
                toValuesString()
        );
    }

    private String toValuesString() {
        StringBuilder strb = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i < values.size()) {
                strb.append(values.get(i));
            } else {
                strb.append("?");
            }
            if (i != columns.size() - 1) {
                strb.append(", ");
            }
        }
        return strb.toString();
    }

    private String toUpdateQueryString() {
        return String.format("UPDATE %s SET %s%s",
                tables.get(0),
                toSetsString(),
                toConditionsString());
    }

    private String toSetsString() {
        StringBuilder strb = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i < values.size()) {
                strb.append(convertToConditionExpression(
                        columns.get(i),
                        ConditionOperator.EQUALS,
                        values.get(i)));
            } else {
                strb.append(columns.get(i)).
                        append(" = ?");
            }
            if (i != columns.size() - 1) {
                strb.append(", ");
            }
        }
        return strb.toString();
    }

    private String toDeleteQueryString() {
        return String.format("DELETE FROM %s%s",
                tables.get(0),
                toConditionsString());
    }

    public enum QueryType {
        SELECT, INSERT, UPDATE, DELETE
    }

    public enum ConditionOperator {
        EQUALS("="), LOWER("<"), GREATER(">"), AND("AND"), OR("OR");

        final String value;

        ConditionOperator(String value) {
            this.value = value;
        }
    }
}
