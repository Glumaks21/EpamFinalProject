package ua.maksym.hlushchenko.dao.db.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlThrowableBiConsumer<V> {
    void accept(V v, Connection connection) throws SQLException;
}
