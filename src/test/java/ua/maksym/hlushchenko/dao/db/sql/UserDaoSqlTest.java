package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.impl.role.Reader;
import ua.maksym.hlushchenko.dao.entity.role.*;
import ua.maksym.hlushchenko.util.Sha256Encoder;

import java.sql.Connection;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDaoSqlTest {
    private static Connection connection;
    private static UserSqlDao dao;
    private static User user;

    static User createUser() {
        User user = new Reader();
        user.setLogin("test");
        user.setPasswordHash(Sha256Encoder.encode("test"));
        return user;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        SqlDaoTestHelper.clearTables();
        connection = HikariCPDataSource.getInstance().getConnection();
        user = createUser();
        dao = new UserSqlDao(connection);
    }

    @Order(1)
    @Test2
    void save() {
        dao.save(user);
        assertTrue(user.getId() != 0);
    }

    @SneakyThrows
    @Order(2)
    @Test2
    void findAll() {
        List<User> users = dao.findAll();
        assertTrue(users.contains(user));
    }

    @Order(3)
    @Test2
    void find() {
        Optional<User> optionalUserInDb = dao.find(user.getId());
        assertTrue(optionalUserInDb.isPresent());
        User userInDb = optionalUserInDb.get();
        assertEquals(user, userInDb);
    }

    @Order(4)
    @Test2
    void findByLogin() {
        Optional<User> optionalUserInDb = dao.findByLogin(user.getLogin());
        assertTrue(optionalUserInDb.isPresent());
        User userInDb = optionalUserInDb.get();
        assertEquals(user, userInDb);
    }

    @Order(5)
    @Test2
    void update() {
        user.setPasswordHash(Sha256Encoder.encode("ne_test"));
        dao.update(user);
        find();
    }

    @Order(6)
    @Test2
    void delete() {
        dao.delete(user.getId());
        Optional<User> userInDb = dao.find(user.getId());
        assertTrue(userInDb.isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        SqlDaoTestHelper.clearTables();
        connection.close();
    }
}