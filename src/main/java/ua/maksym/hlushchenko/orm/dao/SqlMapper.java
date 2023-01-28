package ua.maksym.hlushchenko.orm.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface SqlMapper<T> {
    T map(ResultSet resultSet);
}
