package ua.maksym.hlushchenko.dao.db.sql;

import java.util.*;

public abstract class QueryBuilder<T extends QueryBuilder<?>> {
    private final TableContainer tableContainer = new TableContainer();

    protected TableContainer getTableContainer() {
        return tableContainer;
    }

    public T addMainTable(String mainTable) {
        tableContainer.addMainTable(mainTable);
        return (T) this;
    }

    protected static String convertToTableColumn(String tableName, String columnName) {
        return tableName + "." + columnName;
    }

    protected static String convertToValue(Object object) {
        if (object instanceof String) {
            return "'" + object + "'";
        }
        return Objects.toString(object);
    }

    protected static String convertToConditionExpression
            (String var, ConditionOperator conditionOperator, Object value) {
        return var.trim() + " " + conditionOperator.value + " " + (value == null ? "?" : convertToValue(value));
    }

    protected static class TableContainer {
        private final List<String> tables = new ArrayList<>();

        public void add(String table) {
            if (table == null || table.isBlank()) {
                throw new IllegalArgumentException();
            }

            tables.add(table);
        }

        protected void addMainTable(String mainTable) {
            if (!tables.isEmpty()) {
                throw new IllegalStateException("Main table is already set: " + getMainTable());
            }

            add(mainTable);
        }

        protected String getMainTable() {
            if (tables.isEmpty()) {
                throw new IllegalStateException("Main table is not set");
            }
            return tables.get(0);
        }

        public List<String> getAll() {
            return Collections.unmodifiableList(tables);
        }
    }

    protected static class ColumnContainer {
        private final List<String> columns = new ArrayList<>();
        private final TableContainer tableContainer;

        public ColumnContainer(TableContainer tableContainer) {
            this.tableContainer = tableContainer;
        }

        public void add(String table, String column) {
            if (!tableContainer.getAll().contains(table)) {
                throw new IllegalArgumentException("Table " + table + " is unknown");
            }

            columns.add(column);
        }

        public List<String> getAll() {
            return Collections.unmodifiableList(columns);
        }
    }

    protected static class ValueContainer {
        private final List<String> values = new ArrayList<>();
        private final ColumnContainer columnContainer;

        public ValueContainer(ColumnContainer columnContainer) {
            this.columnContainer = columnContainer;
        }

        public void add(Object value) {
            if (columnContainer.getAll().size() == values.size()) {
                throw new IllegalStateException("There are no columns for extra value: " + value);
            }

            values.add(convertToValue(value));
        }

        public List<String> getAll() {
            return Collections.unmodifiableList(values);
        }
    }

    protected static class ConditionContainer {
        private String conditionString = "";
        private boolean isPrevCondition;

        private final TableContainer tableContainer;

        public ConditionContainer(TableContainer tableContainer) {
            this.tableContainer = tableContainer;
        }

        public void addCondition(String table, String column,
                                               ConditionOperator conditionOperator, Object value) {
            if (!tableContainer.getAll().contains(table)) {
                throw new IllegalArgumentException("Table " + table + " is unknown");
            } else if (isPrevCondition) {
                throw new IllegalStateException("Condition concatenation wasn't added");
            }

            String fullTableName = convertToTableColumn(table, column);
            String condExp = convertToConditionExpression(fullTableName, conditionOperator, value);
            conditionString += condExp;
            isPrevCondition = true;
        }

        public void addConditionConcatenation(ConditionOperator conditionOperator) {
            if (!isPrevCondition) {
                throw new IllegalStateException("Condition concatenation has already added");
            }

            conditionString += " " + conditionOperator + " ";
            isPrevCondition = false;
        }

        public String getWhereString() {
            if (!isPrevCondition && !conditionString.equals("")) {
                throw new IllegalStateException("Condition wasn't added after concatenation");
            }

            return conditionString.equals("")? "": " WHERE " + conditionString;
        }
    }
}
