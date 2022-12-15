package ua.maksym.hlushchenko.dao.db.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlBiConsumer<Q> {
    void accept(Q q, Connection connection) throws SQLException;
}
