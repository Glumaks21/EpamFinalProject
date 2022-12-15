package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;

import ua.maksym.hlushchenko.dao.entity.impl.role.AdminImpl;
import ua.maksym.hlushchenko.dao.entity.role.Admin;

import javax.sql.DataSource;
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

    public AdminSqlDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected Admin mapToEntity(ResultSet resultSet) throws SQLException {
        Admin admin = new AdminImpl();
        admin.setLogin(resultSet.getString("login"));
        admin.setPassword(resultSet.getString("password"));
        return admin;
    }

    @Override
    public List<Admin> findAll() {
        return mappedQueryResult(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Admin> find(String login) {
        List<Admin> admins = mappedQueryResult(SQL_SELECT_BY_LOGIN, login);
        if (admins.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(admins.get(0));
    }

    @Override
    public void save(Admin admin) {
        dmlOperation(AdminSqlDao::saveInTransaction, admin);
    }

    @Override
    public void update(Admin admin) {}

    @Override
    public void delete(String id) {
        dmlOperation(AdminSqlDao::deleteInTransaction, id);
    }

    static void saveInTransaction(Admin admin, Connection connection) throws SQLException {
        UserSqlDao.saveInTransaction(admin, connection);

        PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        fillPreparedStatement(statement, admin.getLogin());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void deleteInTransaction(String id, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_LOGIN);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        UserSqlDao.deleteInTransaction(id, connection);
    }
}
