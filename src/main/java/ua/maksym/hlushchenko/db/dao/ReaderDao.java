package ua.maksym.hlushchenko.db.dao;

import ua.maksym.hlushchenko.db.entity.roles.Librarian;
import ua.maksym.hlushchenko.db.entity.roles.Reader;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReaderDao extends AbstractSqlDao<String, Reader> {
    static String SQL_SELECT_ALL = "SELECT * FROM reader r " +
            "JOIN user u ON r.user_login = u.login";
    static String SQL_SELECT_BY_LOGIN = "SELECT * FROM reader r " +
            "JOIN user u ON r.user_login = u.login WHERE login = ?";
    static String SQL_INSERT = "INSERT INTO reader(user_login, blocked) " +
            "VALUES(?, ?)";
    static String SQL_UPDATE_BY_LOGIN = "UPDATE reader SET blocked = ? WHERE user_login = ?";
    static String SQL_DELETE_BY_LOGIN = "DELETE FROM reader WHERE user_login = ?";

    public ReaderDao(Connection connection) {
        super(connection);
    }

    static Reader mapToReader(ResultSet resultSet) throws SQLException {
        Reader reader = new Reader();
        reader.setBlocked(resultSet.getBoolean("blocked"));
        reader.setUser(UserDao.mapToUser(resultSet));
        return reader;
    }

    @Override
    public List<Reader> findAll() {
        List<Reader> readers = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);

            while (resultSet.next()) {
                Reader reader = mapToReader(resultSet);
                readers.add(reader);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return readers;
    }

    @Override
    public Optional<Reader> find(String id) {
        Reader reader = null;
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_LOGIN);
            fillPreparedStatement(statement, id);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                reader = mapToReader(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.ofNullable(reader);
    }

    @Override
    public void save(Reader reader) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(UserDao.SQL_INSERT);
            fillPreparedStatement(statement,
                    reader.getUser().getLogin(),
                    reader.getUser().getPassword());
            statement.executeUpdate();

            statement = connection.prepareStatement(SQL_INSERT);
            fillPreparedStatement(statement,
                    reader.getUser().getLogin(),
                    reader.isBlocked());
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            tryToRollBack(connection);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Reader reader) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_LOGIN);
            fillPreparedStatement(statement,
                    reader.isBlocked(),
                    reader.getUser().getLogin());
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            tryToRollBack(connection);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String id) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_LOGIN);
            fillPreparedStatement(statement, id);
            statement.executeUpdate();

            statement = connection.prepareStatement(UserDao.SQL_DELETE_BY_LOGIN);
            fillPreparedStatement(statement, id);
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            tryToRollBack(connection);
            throw new RuntimeException(e);
        }
    }
}
