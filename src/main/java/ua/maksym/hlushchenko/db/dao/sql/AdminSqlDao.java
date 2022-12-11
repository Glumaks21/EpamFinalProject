package ua.maksym.hlushchenko.db.dao.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.db.entity.impl.role.AdminImpl;
import ua.maksym.hlushchenko.db.entity.role.Admin;

import java.sql.*;
import java.util.*;

public class AdminSqlDao extends AbstractSqlDao<String, Admin> {
    private static final String SQL_SELECT_ALL = "SELECT * FROM admin a " +
            "JOIN user u ON a.user_login = u.login";
    private static final String SQL_SELECT_BY_LOGIN = "SELECT * FROM admin a " +
            "JOIN user u ON a.user_login = u.login " +
            "WHERE login = ?";
    private static final String SQL_INSERT = "INSERT INTO admin(user_login) " +
            "VALUES(?)";
    private static final String SQL_DELETE_BY_LOGIN = "DELETE FROM admin " +
            "WHERE user_login = ?";

    private static final Logger log = LoggerFactory.getLogger(AdminSqlDao.class);

    public AdminSqlDao(Connection connection) {
        super(connection);
    }

    private Admin mapToAdmin(ResultSet resultSet) throws SQLException {
        Admin admin = new AdminImpl();
        admin.setLogin(resultSet.getString("login"));
        admin.setPassword(resultSet.getString("password"));
        return admin;
    }

    @Override
    public List<Admin> findAll() {
        List<Admin> admins = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            log.info("Try to execute:\n" + formatSql(SQL_SELECT_ALL));

            ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);
            while (resultSet.next()) {
                Admin admin = mapToAdmin(resultSet);
                admins.add(admin);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }
        return admins;
    }

    @Override
    public Optional<Admin> find(String id) {
        Admin admin = null;
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_LOGIN);
            fillPreparedStatement(statement, id);
            log.info("Try to execute:\n" + formatSql(statement));

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                admin = mapToAdmin(resultSet);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }
        return Optional.ofNullable(admin);
    }

    @Override
    public void save(Admin admin) {
        try  {
            connection.setAutoCommit(false);
            saveInSession(admin, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    @Override
    public void update(Admin admin) {}

    @Override
    public void delete(String id) {
        try  {
            connection.setAutoCommit(false);
            deleteInSession(id, connection);
            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack();
        }
    }

    static void saveInSession(Admin admin, Connection connection) throws SQLException {
        UserSqlDao.saveInSession(admin, connection);

        PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        fillPreparedStatement(statement, admin.getLogin());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void deleteInSession(String id, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_LOGIN);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        UserSqlDao.deleteInSession(id, connection);
    }
}
