package ua.maksym.hlushchenko.dao.db.sql;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InsertQueryBuilder extends QueryBuilder<InsertQueryBuilder>
        implements QueryBuilderWithValues<InsertQueryBuilder> {
    private final ColumnContainer columns = new ColumnContainer(getTableContainer());
    private final ValueContainer values = new ValueContainer(columns);


    @Override
    public InsertQueryBuilder addColumn(String table, String column) {
        columns.add(table, column);
        return this;
    }

    @Override
    public InsertQueryBuilder addValue(Object value) {
        values.add(value);
        return this;
    }

    @Override
    public String toString() {
        if (columns.getAll().isEmpty()) {
            throw new IllegalStateException("Columns wasn't added to insert");
        }
        return String.format("INSERT INTO %s(%s) VALUES(%s)",
                getTableContainer().getMainTable(),
                String.join(", ", columns.getAll()),
                getValues()
        );
    }

    private String getValues() {
        List<String> columnList = columns.getAll();
        List<String> valueList = values.getAll();
        return IntStream.range(0, columnList.size()).boxed().
                map(ind ->  ind < valueList.size()? valueList.get(ind): "?").
                collect(Collectors.joining(", "));
    }
}
