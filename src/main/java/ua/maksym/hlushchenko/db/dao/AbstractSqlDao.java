package ua.maksym.hlushchenko.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractSqlDao<K, T> implements Dao<K, T> {
    protected Connection connection;

    public AbstractSqlDao(Connection connection) {
        Objects.requireNonNull(connection);
        this.connection = connection;
    }

    @Override
    public abstract List<T> findAll();

    @Override
    public abstract Optional<T> find(K id);

    @Override
    public abstract void save(T entity);

    @Override
    public abstract void update(T entity);

    @Override
    public abstract void delete(K id);

    protected void tryToRollBack(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    protected void fillPreparedStatement(PreparedStatement statement, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
