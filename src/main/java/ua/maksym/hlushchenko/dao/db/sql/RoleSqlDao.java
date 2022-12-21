package ua.maksym.hlushchenko.dao.db.sql;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.entity.impl.role.RoleImpl;
import ua.maksym.hlushchenko.dao.entity.role.Role;
import ua.maksym.hlushchenko.exception.DaoException;
import ua.maksym.hlushchenko.exception.MappingException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;

public class RoleSqlDao extends AbstractSqlDao<Integer, Role> {
    static final String SQL_SELECT_ALL = "SELECT id, name " +
            "FROM role";
    static final String SQL_SELECT_BY_ID = "SELECT id, name " +
            "FROM role " +
            "WHERE id = ?";
    static final String SQL_INSERT = "INSERT INTO role(name) " +
            "VALUES(?)";
    static final String SQL_UPDATE_BY_ID = "UPDATE role " +
            "SET name = ? " +
            "WHERE id = ?";
    static final String SQL_DELETE_BY_ID = "DELETE FROM role " +
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
            return (Role) Proxy.newProxyInstance(
                    RoleSqlDao.class.getClassLoader(),
                    new Class[]{Role.class, LoadProxy.class},
                    new LoadHandler<>(role));
        } catch (SQLException e) {
            throw new MappingException(e);
        }
    }

    @Override
    public List<Role> findAll() {
        return mappedQuery(SQL_SELECT_ALL);
    }

    @Override
    public Optional<Role> find(Integer id) {
        List<Role> roles = mappedQuery(SQL_SELECT_BY_ID, id);
        if (roles.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(roles.get(0));
    }

    @Override
    public void save(Role role) {
        try (ResultSet resultSet = updateQuery(SQL_INSERT, role.getName())) {
            if (resultSet.next()) {
                role.setId(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void update(Role role) {
        updateQuery(SQL_UPDATE_BY_ID,
                role.getName(),
                role.getId());
    }

    @Override
    public void delete(Integer id) {
        updateQuery(SQL_DELETE_BY_ID, id);
    }
}
