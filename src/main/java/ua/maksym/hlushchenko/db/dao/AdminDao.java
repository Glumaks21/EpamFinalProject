package ua.maksym.hlushchenko.db.dao;

import ua.maksym.hlushchenko.db.entity.Admin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminDao extends AbstractSqlDao<String, Admin> {
    private static final String SQL_SELECT_ALL = "SELECT * FROM admin a " +
            "JOIN user u ON a.user_login = u.login";
    private static final String SQL_SELECT_BY_LOGIN = "SELECT * FROM admin a " +
            "JOIN user u ON a.user_login = u.login WHERE login = ?";
    private static final String SQL_INSERT = "INSERT INTO admin(user_login) VALUES(?)";
    private static final String SQL_DELETE_BY_LOGIN = "DELETE FROM admin WHERE user_login =?";

    public AdminDao(Connection connection) {
        super(connection);
    }

    static Admin mapToAdmin(ResultSet resultSet) throws SQLException {
        Admin admin = new Admin();
        admin.setUser(UserDao.mapToUser(resultSet));
        return admin;
    }

    @Override
    public List<Admin> findAll() {
        List<Admin> admins = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);

            while (resultSet.next()) {
                Admin admin = mapToAdmin(resultSet);
                admins.add(admin);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return admins;
    }

    @Override
    public Optional<Admin> find(String id) {
        Admin admin = null;
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_LOGIN);
            fillPreparedStatement(statement, id);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                admin = mapToAdmin(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.ofNullable(admin);
    }

    @Override
    public void save(Admin admin) {
        try  {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(UserDao.SQL_INSERT);
            fillPreparedStatement(statement,
                    admin.getUser().getLogin(),
                    admin.getUser().getPassword());
            statement.executeUpdate();

            statement = connection.prepareStatement(SQL_INSERT);
            fillPreparedStatement(statement, admin.getUser().getLogin());
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            tryToRollBack(connection);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Admin entity) {}

    @Override
    public void delete(String id) {
        try  {
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

