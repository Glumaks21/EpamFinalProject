package ua.maksym.hlushchenko.dao.db.sql;

import java.util.List;

public interface QueryBuilderWithValues<T extends QueryBuilderWithValues<?>> extends QueryBuilderWithColumns<T> {
    T addValue(Object value);

     default T addAllValues(List<?> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException();
        }
        values.forEach(this::addValue);
        return (T) this;
    }
}
