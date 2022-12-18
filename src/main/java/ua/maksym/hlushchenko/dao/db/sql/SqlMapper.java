package ua.maksym.hlushchenko.dao.db.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface SqlMapper<T> {
    T map(ResultSet resultSet);
}
