package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.impl.role.Admin;
import ua.maksym.hlushchenko.util.Sha256Encoder;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminSqlDaoTest {
    private static Connection connection;
    private static Dao<Integer, Admin> dao;
    private static Admin admin;

    static Admin createAdmin() {
        Admin admin = new Admin();
        admin.setLogin("librarian");
        admin.setPasswordHash(Sha256Encoder.encode("It is true"));
        return admin;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        SqlDaoTestHelper.clearTables();
        connection = HikariCPDataSource.getInstance().getConnection();
        dao = new GenericDao<>(Admin.class, connection);
        admin = createAdmin();
    }

    @Order(1)
    @Test2
    void save() {
        dao.save(admin);
        assertTrue(admin.getId() > 0);
    }

    @Order(2)
    @Test2
    void findAll() {
        List<Admin> admins = dao.findAll();
        assertTrue(admins.contains(admin));
    }

    @Order(3)
    @Test2
    void find() {
        Optional<Admin> optionalAdmin = dao.find(admin.getId());
        assertTrue(optionalAdmin.isPresent());
        Admin adminInDb = optionalAdmin.get();
        assertEquals(admin, adminInDb);
    }

    @Order(4)
    @Test2
    void update() {
        admin.setPasswordHash(Sha256Encoder.encode("This is was joke"));
        dao.update(admin);
        find();
    }

    @Order(5)
    @Test2
    void delete() {
        dao.delete(admin.getId());
        assertTrue(dao.find(admin.getId()).isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        SqlDaoTestHelper.clearTables();
        connection.close();
    }
}