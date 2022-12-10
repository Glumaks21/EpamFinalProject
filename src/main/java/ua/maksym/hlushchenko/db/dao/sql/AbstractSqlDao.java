package ua.maksym.hlushchenko.db.dao.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.maksym.hlushchenko.db.dao.Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public abstract class AbstractSqlDao<K, T> implements Dao<K, T>, AutoCloseable {
    protected Connection connection;

    private static final Logger log = LoggerFactory.getLogger(AbstractSqlDao.class);

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


    protected void tryToRollBack() {
        if (connection != null) {
            log.info("Try to roll back");
            try {
                connection.rollback();
            } catch (SQLException e) {
                log.warn(e.getMessage());
            }
            log.info("Roll back successful");
        }
    }

    protected void fillPreparedStatement(PreparedStatement statement, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    protected String formatSql(Statement statement) {
        Objects.requireNonNull(statement);
        String dirtySqlQuery = statement.toString();
        return formatSql(dirtySqlQuery.substring(dirtySqlQuery.indexOf(": ") + 1));
    }

    protected String formatSql(String sqlQuery) {
        if (sqlQuery == null || sqlQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL query is " + sqlQuery);
        }

        StringBuilder strb = new StringBuilder();
        String[] words = sqlQuery.trim().split("\\s+");

        for (int i = 0; i < words.length - 1; i++) {
            strb.append(words[i]);

            String nextWord = words[i + 1];
            if (nextWord.equalsIgnoreCase("JOIN") ||
                    nextWord.equalsIgnoreCase("WHERE")) {
                strb.append("\n");
            } else {
                strb.append(" ");
            }
        }

        strb.append(words[words.length - 1]);
        return strb.toString();
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
