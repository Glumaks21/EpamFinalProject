package ua.maksym.hlushchenko.dao.db.sql;


import java.util.List;

public final class SelectQueryBuilder extends QueryBuilder<SelectQueryBuilder>
        implements QueryBuilderWithConditions<SelectQueryBuilder>, QueryBuilderWithColumns<SelectQueryBuilder> {

    private final ColumnContainer columns = new ColumnContainer(getTableContainer());
    private final ConditionContainer conditions = new ConditionContainer(getTableContainer());
    private String joins = "";

    public SelectQueryBuilder addColumn(String table, String column) {
        column = convertToTableColumn(table, column) + " AS " + convertToResultSetColumn(table, column);
        columns.add(table, column);
        return this;
    }

    public static String convertToResultSetColumn(String table, String column) {
        return "_" + table + "_" + column;
    }

    public SelectQueryBuilder addCondition(String table, String column,
                                           ConditionOperator conditionOperator, Object value) {
        conditions.addCondition(table, column, conditionOperator, value);
        return this;
    }

    public SelectQueryBuilder addConditionConcatenation(ConditionOperator conditionOperator) {
        conditions.addConditionConcatenation(conditionOperator);
        return this;
    }

    public SelectQueryBuilder addJoin(String table, String joinTable, String column, String joinColumn) {
        if (!getTableContainer().getAll().contains(table)) {
            throw new IllegalArgumentException("Table " + table + " is unknown");
        }

        getTableContainer().add(joinTable);
        String join = " JOIN " + joinTable + " ON " +
                convertToTableColumn(table, column) + " = " + convertToTableColumn(joinTable, joinColumn);
        joins += join;
        return this;
    }

    @Override
    public String toString() {
        List<String> columnList = columns.getAll();
        if (columnList.isEmpty()) {
            throw new IllegalStateException("Columns weren't added");
        }
        return "SELECT " + String.join(", ", columnList) +
                " FROM " + getTableContainer().getMainTable() +
                joins + conditions.getWhereString();
    }
}
