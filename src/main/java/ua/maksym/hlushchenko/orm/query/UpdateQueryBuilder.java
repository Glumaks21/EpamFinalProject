package ua.maksym.hlushchenko.orm.query;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UpdateQueryBuilder extends QueryBuilder<UpdateQueryBuilder>
        implements QueryBuilderWithColumns<UpdateQueryBuilder>, QueryBuilderWithConditions<UpdateQueryBuilder> {
    private final ColumnContainer columns = new ColumnContainer(getTableContainer());
    private final ConditionContainer conditions = new ConditionContainer(getTableContainer());


    @Override
    public UpdateQueryBuilder addColumn(String table, String column) {
        columns.add(table, column);
        return this;
    }

    @Override
    public UpdateQueryBuilder addCondition(String table, String column,
                                           ConditionOperator conditionOperator, Object value) {
        conditions.addCondition(table, column, conditionOperator, value);
        return this;
    }

    @Override
    public UpdateQueryBuilder addConditionConcatenation(ConditionOperator conditionOperator) {
        conditions.addConditionConcatenation(conditionOperator);
        return this;
    }

    @Override
    public String toString() {
        List<String> columnList = columns.getAll();
        if (columnList.isEmpty()) {
            throw new IllegalStateException("Columns weren't added");
        }
        return "UPDATE " + getTableContainer().getMainTable() +
                getSet() + conditions.getWhereString();
    }

    private String getSet() {
        return columns.getAll().stream().
                map(column -> column + " = ?").
                collect(Collectors.joining(", ", " SET ", ""));
    }
}
