package ua.maksym.hlushchenko.db.dao;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.db.HikariCPDataSource;
import ua.maksym.hlushchenko.db.entity.roles.Admin;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminDaoTest {
    private static Connection connection;
    private static AdminDao dao;
    private static Admin admin;

    static Admin createAdmin() {
        Admin admin = new Admin();
        admin.setUser(UserDaoTest.createUser());
        return admin;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        connection = HikariCPDataSource.getConnection();
        dao = new AdminDao(connection);
        admin = createAdmin();
    }

    @Order(1)
    @Test
    void save() {
        dao.save(admin);
    }

    @SneakyThrows
    @Order(2)
    @Test
    void findAll() {
        List<Admin> admins = dao.findAll();
        Assertions.assertTrue(admins.contains(admin));
    }

    @Order(3)
    @Test
    void find() {
        Admin adminInDb = dao.find(admin.getUser().getLogin()).get();
        Assertions.assertEquals(adminInDb, admin);
    }

    @Order(4)
    @Test
    void delete() {
        dao.delete(admin.getUser().getLogin());
        Optional<Admin> userInDb = dao.find(admin.getUser().getLogin());
        Assertions.assertTrue(userInDb.isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        connection.close();
    }
}