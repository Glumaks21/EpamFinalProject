package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.entity.impl.role.UserImpl;
import ua.maksym.hlushchenko.dao.entity.role.*;
import ua.maksym.hlushchenko.util.Sha256Encoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDaoSqlTest {
    private static SqlDaoFactory sqlDaoFactory;
    private static UserSqlDao dao;
    private static User user;

    static User createUser() {
        User user = new UserImpl();
        user.setLogin("test");
        user.setPasswordHash(Sha256Encoder.encode("test"));
        user.setRole(RoleSqlDaoTest.createRole());
        return user;
    }

    @BeforeAll
    static void init() {
        SqlDaoTestHelper.clearTables();
        sqlDaoFactory = new SqlDaoFactory();

        RoleSqlDao roleSqlDao = sqlDaoFactory.createRoleDao();
        Role role = RoleSqlDaoTest.createRole();
        roleSqlDao.save(role);

        user = createUser();
        user.setRole(role);
        dao = sqlDaoFactory.createUserDao();
    }

    @Order(1)
    @Test
    void save() {
        dao.save(user);
        assertTrue(user.getId() != 0);
    }

    @SneakyThrows
    @Order(2)
    @Test
    void findAll() {
        List<User> users = dao.findAll();
        assertTrue(users.contains(user));
    }

    @Order(3)
    @Test
    void find() {
        Optional<User> optionalUserInDb = dao.find(user.getId());
        assertTrue(optionalUserInDb.isPresent());
        User userInDb = optionalUserInDb.get();
        assertEquals(user, userInDb);
    }

    @Order(4)
    @Test
    void update() {
        user.setPasswordHash(Sha256Encoder.encode("ne_test"));
        dao.update(user);
        find();
    }

    @Order(5)
    @Test
    void delete() {
        dao.delete(user.getId());
        Optional<User> userInDb = dao.find(user.getId());
        assertTrue(userInDb.isEmpty());
    }

    @AfterAll
    static void destroy() {
        SqlDaoTestHelper.clearTables();
        sqlDaoFactory.close();
    }
}