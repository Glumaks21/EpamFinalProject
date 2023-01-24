package ua.maksym.hlushchenko.dao.db.sql;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UpdateQueryBuilder extends QueryBuilder<UpdateQueryBuilder>
        implements QueryBuilderWithValues<UpdateQueryBuilder>, QueryBuilderWithConditions<UpdateQueryBuilder> {
    private final ColumnContainer columns = new ColumnContainer(getTableContainer());
    private final ValueContainer values = new ValueContainer(columns);
    private final ConditionContainer conditions = new ConditionContainer(getTableContainer());


    @Override
    public UpdateQueryBuilder addColumn(String table, String column) {
        columns.add(table, column);
        return this;
    }

    @Override
    public UpdateQueryBuilder addValue(Object value) {
        values.add(value);
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
        return "UPDATE " + getTableContainer().getMainTable() +
                getSet() + conditions.getWhereString();
    }

    private String getSet() {
        List<String> columnList = columns.getAll();
        if (columnList.isEmpty()) {
            throw new IllegalStateException("Columns weren't added");
        }
        List<String> valueList = values.getAll();
        return IntStream.range(0, columnList.size()).boxed().
                map(ind -> columnList.get(ind) + " = " + (ind < valueList.size()? valueList.get(ind): "?")).
                collect(Collectors.joining(", ", " SET ", ""));
    }
}
