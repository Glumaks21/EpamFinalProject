package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.impl.role.AdminImpl;
import ua.maksym.hlushchenko.dao.entity.role.Admin;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminSqlDaoTest {
    private static AdminSqlDao dao;
    private static AdminImpl admin;

    static AdminImpl createAdmin() {
        AdminImpl admin = new AdminImpl();
        admin.setLogin("test");
        admin.setPassword("test");
        return admin;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        dao = new AdminSqlDao(HikariCPDataSource.getInstance());
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
        Optional<Admin> optionalAdminInDb = dao.find(admin.getLogin());
        Assertions.assertTrue(optionalAdminInDb.isPresent());
        Admin adminInDb = optionalAdminInDb.get();
        Assertions.assertEquals(adminInDb, admin);
    }

    @Order(4)
    @Test
    void delete() {
        dao.delete(admin.getLogin());
        Optional<Admin> userInDb = dao.find(admin.getLogin());
        Assertions.assertTrue(userInDb.isEmpty());
    }
}