package ua.maksym.hlushchenko.db.dao.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.db.entity.roles.Admin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class AdminSqlDao extends AbstractSqlDao<String, Admin> {
    static final String SQL_SELECT_ALL = "SELECT * FROM admin a " +
            "JOIN user u ON a.user_login = u.login";
    static final String SQL_SELECT_BY_LOGIN = "SELECT * FROM admin a " +
            "JOIN user u ON a.user_login = u.login WHERE login = ?";
    static final String SQL_INSERT = "INSERT INTO admin(user_login) VALUES(?)";
    static final String SQL_DELETE_BY_LOGIN = "DELETE FROM admin WHERE user_login = ?";

    private static final Logger log = LoggerFactory.getLogger(AdminSqlDao.class);

    public AdminSqlDao(Connection connection) {
        super(connection);
    }

    static Admin mapToAdmin(ResultSet resultSet) throws SQLException {
        Admin admin = new Admin();
        admin.setUser(UserSqlDao.mapToUser(resultSet));
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

            PreparedStatement statement = connection.prepareStatement(UserSqlDao.SQL_INSERT);
            fillPreparedStatement(statement,
                    admin.getUser().getLogin(),
                    admin.getUser().getPassword());
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            statement = connection.prepareStatement(SQL_INSERT);
            fillPreparedStatement(statement, admin.getUser().getLogin());
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack(connection);
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
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            statement = connection.prepareStatement(UserSqlDao.SQL_DELETE_BY_LOGIN);
            fillPreparedStatement(statement, id);
            log.info("Try to execute:\n" + formatSql(statement));
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            log.warn(e.getMessage());
            tryToRollBack(connection);
        }
    }
}

