package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.impl.role.RoleImpl;
import ua.maksym.hlushchenko.dao.entity.role.Role;
import ua.maksym.hlushchenko.exception.MappingException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class RoleSqlDao extends AbstractSqlDao<Integer, Role> {
    private static final String SQL_SELECT_ALL = "SELECT id, name FROM role";
    private static final String SQL_SELECT_BY_ID = "SELECT id, name FROM role " +
            "WHERE id = ?";
    private static final String SQL_INSERT = "INSERT INTO role(name) " +
            "VALUES(?)";
    private static final String SQL_UPDATE_BY_ID = "UPDATE role " +
            "SET name = ? " +
            "WHERE id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM role " +
            "WHERE id = ?";

    private static final Logger log = LoggerFactory.getLogger(RoleSqlDao.class);

    public RoleSqlDao(Connection connection) {
        super(connection);
    }

    @Override
    protected Role mapToEntity(ResultSet resultSet) {
        try {
            Role role = new RoleImpl();
            role.setId(resultSet.getInt("id"));
            role.setName(resultSet.getString("name"));
            return role;
        } catch (SQLException e) {
            throw new MappingException(e);
        }
    }

    @Override
    public List<Role> findAll() {
        return mappedQueryResult(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Role> find(Integer id) {
        List<Role> roles = mappedQueryResult(SQL_SELECT_BY_ID, id);
        if (roles.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(roles.get(0));
    }

    @Override
    public void save(Role role) {
        updateInTransaction(RoleSqlDao::saveInTransaction, role);
    }

    @Override
    public void update(Role role) {
        updateInTransaction(RoleSqlDao::updateInTransaction, role);
    }

    @Override
    public void delete(Integer id) {
        updateInTransaction(RoleSqlDao::deleteInTransaction, id);
    }

    static void saveInTransaction(Role role, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT,
                Statement.RETURN_GENERATED_KEYS);
        fillPreparedStatement(statement, role.getName());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();

        ResultSet resultSet = statement.getGeneratedKeys();
        if (resultSet.next()) {
            role.setId(resultSet.getInt(1));
        }
    }

    static void updateInTransaction(Role role, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_ID);
        fillPreparedStatement(statement, role.getName(), role.getId());
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }

    static void deleteInTransaction(Integer id, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ID);
        fillPreparedStatement(statement, id);
        log.info("Try to execute:\n" + formatSql(statement));
        statement.executeUpdate();
    }
}
