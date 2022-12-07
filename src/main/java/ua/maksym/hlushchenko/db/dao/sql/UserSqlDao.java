package ua.maksym.hlushchenko.db.dao.sql;

import ua.maksym.hlushchenko.db.entity.roles.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserSqlDao extends AbstractSqlDao<String, User> {
    static final String SQL_SELECT_ALL = "SELECT * FROM user";
    static final String SQL_SELECT_BY_LOGIN = "SELECT * FROM user WHERE login = ?";
    static final String SQL_INSERT = "INSERT INTO user(login, password) VALUES(?, ?)";
    static final String SQL_UPDATE_BY_LOGIN = "UPDATE user SET password = ? WHERE login = ?";
    static final String SQL_DELETE_BY_LOGIN = "DELETE FROM user WHERE login = ?";

    public UserSqlDao(Connection connection) {
        super(connection);
    }

    static User mapToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setLogin(resultSet.getString("login"));
        user.setPassword(resultSet.getString("password"));
        return user;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);

            while (resultSet.next()) {
                User user = mapToUser(resultSet);
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return users;
    }

    @Override
    public Optional<User> find(String id) {
        User user = null;
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_LOGIN);
            fillPreparedStatement(statement, id);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                user = mapToUser(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.ofNullable(user);
    }

    @Override
    public void save(User user) {
        try  {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
            fillPreparedStatement(statement,
                    user.getLogin(),
                    user.getPassword());
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            tryToRollBack(connection);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(User user) {
        try  {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_LOGIN);
            fillPreparedStatement(statement,
                    user.getPassword(),
                    user.getLogin());
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            tryToRollBack(connection);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String id) {
        try  {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_LOGIN);
            fillPreparedStatement(statement, id);
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            tryToRollBack(connection);
            throw new RuntimeException(e);
        }
    }
}
