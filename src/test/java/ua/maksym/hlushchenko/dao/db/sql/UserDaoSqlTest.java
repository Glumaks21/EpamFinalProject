package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.impl.role.UserImpl;
import ua.maksym.hlushchenko.dao.entity.role.User;


import java.sql.Connection;
import java.util.List;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDaoSqlTest {
    private static UserSqlDao dao;
    private static User user;

    static User createUser() {
        UserImpl user = new UserImpl();
        user.setLogin("test");
        user.setPassword("test");
        return user;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        dao = new UserSqlDao(HikariCPDataSource.getInstance());
        user = createUser();
    }

    @Order(1)
    @Test
    void save() {
        dao.save(user);
    }

    @SneakyThrows
    @Order(2)
    @Test
    void findAll() {
        List<User> users = dao.findAll();
        Assertions.assertTrue(users.contains(user));
    }

    @Order(3)
    @Test
    void find() {
        Optional<User> optionalUserInDb = dao.find(user.getLogin());
        Assertions.assertTrue(optionalUserInDb.isPresent());
        User userInDb = optionalUserInDb.get();
        Assertions.assertEquals(userInDb, user);
    }

    @Order(4)
    @Test
    void update() {
        user.setPassword("ne_test");
        dao.update(user);
        find();
    }

    @Order(5)
    @Test
    void delete() {
        dao.delete(user.getLogin());
        Optional<User> userInDb = dao.find(user.getLogin());
        Assertions.assertTrue(userInDb.isEmpty());
    }
}