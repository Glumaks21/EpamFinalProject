package ua.maksym.hlushchenko.db.dao.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.db.HikariCPDataSource;
import ua.maksym.hlushchenko.db.entity.model.role.UserModel;
import ua.maksym.hlushchenko.db.entity.role.User;


import java.sql.Connection;
import java.util.List;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDaoSqlTest {
    private static Connection connection;
    private static UserSqlDao dao;
    private static UserModel user;

    static UserModel createUser() {
        UserModel user = new UserModel();
        user.setLogin("test");
        user.setPassword("test");
        return user;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        connection = HikariCPDataSource.getConnection();
        dao = new UserSqlDao(connection);
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

    @SneakyThrows
    @AfterAll
    static void destroy() {
        connection.close();
    }
}