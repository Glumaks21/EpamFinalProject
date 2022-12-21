package ua.maksym.hlushchenko.dao.db.sql;

import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.entity.impl.role.RoleImpl;
import ua.maksym.hlushchenko.dao.entity.role.Role;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoleSqlDaoTest {
    private static SqlDaoFactory sqlDaoFactory;
    private static RoleSqlDao dao;
    private static Role role;

    static Role createRole() {
        Role role = new RoleImpl();
        role.setName("test");
        return role;
    }

    @BeforeAll
    static void init() {
        SqlDaoTestHelper.clearTables();
        sqlDaoFactory = new SqlDaoFactory();
        dao = sqlDaoFactory.createRoleDao();
        role = createRole();
    }

    @Order(1)
    @Test
    void save() {
        dao.save(role);
        assertTrue(role.getId() != 0);
    }

    @Order(2)
    @Test
    void findAll() {
        List<Role> roles = dao.findAll();
        assertTrue(roles.contains(role));
    }

    @Order(3)
    @Test
    void find() {
        Optional<Role> optionalRoleInDb = dao.find(role.getId());
        Assertions.assertTrue(optionalRoleInDb.isPresent());
        Role roleInDb = optionalRoleInDb.get();
        Assertions.assertEquals(role, roleInDb);
    }

    @Order(4)
    @Test
    void update() {
        role.setName("ne_test");
        dao.update(role);
        find();
    }

    @Order(5)
    @Test
    void delete() {
        dao.delete(role.getId());
        assertTrue(dao.find(role.getId()).isEmpty());
    }

    @AfterAll
    static void destroy() {
        SqlDaoTestHelper.clearTables();
        sqlDaoFactory.close();
    }
}
