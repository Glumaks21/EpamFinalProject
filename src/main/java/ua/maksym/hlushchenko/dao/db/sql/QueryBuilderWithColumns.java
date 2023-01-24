package ua.maksym.hlushchenko.dao.db.sql;

import java.util.List;

public interface QueryBuilderWithColumns<T extends QueryBuilderWithColumns<?>> {
    T addColumn(String table, String column);

    default T addAllColumns(String table, List<String> columns) {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Columns: " + columns);
        }

        columns.forEach((column) -> addColumn(table, column));
        return (T) this;
    }
}
