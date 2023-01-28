package ua.maksym.hlushchenko.orm.query;

import java.util.List;

public interface QueryBuilderWithColumns<T extends QueryBuilderWithColumns<?>> {
    T addColumn(String table, String column);

    default T addAllColumns(String table, List<String> columns) {
        columns.forEach((column) -> addColumn(table, column));
        return (T) this;
    }
}
