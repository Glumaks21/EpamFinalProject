package ua.maksym.hlushchenko.orm.query;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InsertQueryBuilder extends QueryBuilder<InsertQueryBuilder>
        implements QueryBuilderWithColumns<InsertQueryBuilder> {
    private final ColumnContainer columns = new ColumnContainer(getTableContainer());

    @Override
    public InsertQueryBuilder addColumn(String table, String column) {
        columns.add(table, column);
        return this;
    }

    @Override
    public String toString() {
        List<String> columnList = columns.getAll();
        if (columnList.isEmpty()) {
            throw new IllegalStateException("Columns wasn't added to insert");
        }
        return String.format("INSERT INTO %s(%s) VALUES(%s)",
                getTableContainer().getMainTable(),
                String.join(", ", columnList),
                getValues()
        );
    }

    private String getValues() {
        return columns.getAll().stream().
                map(ind -> "?").
                collect(Collectors.joining(", "));
    }
}
