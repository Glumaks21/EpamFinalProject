package ua.maksym.hlushchenko.orm.dao;

import java.sql.ResultSet;

public interface Session {
    void command(String query);
    ResultSet query(String query, Object... args);
    void update(String query, Object... args);
    ResultSet updateWithKeys(String query, Object... args);
    void commit();
}
