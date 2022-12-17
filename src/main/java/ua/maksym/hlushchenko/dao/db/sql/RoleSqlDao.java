package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.entity.impl.role.RoleImpl;
import ua.maksym.hlushchenko.dao.entity.role.Role;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class RoleSqlDao extends AbstractSqlDao<Integer, Role> {
    private static final String SQL_SELECT_ALL = "SELECT id, name FROM role";
    private static final String SQL_SELECT_BY_ID = "SELECT id, name FROM role " +
            "WHERE id = ?";

    public RoleSqlDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected Role mapToEntity(ResultSet resultSet) throws SQLException {
        Role role = new RoleImpl();
        role.setId(resultSet.getInt("id"));
        role.setName(resultSet.getString("name"));
        return role;
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
    public void save(Role entity) {}

    @Override
    public void update(Role entity) {}

    @Override
    public void delete(Integer id) {}
}
