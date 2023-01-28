package ua.maksym.hlushchenko.dao.db.sql;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.orm.dao.Dao;
import ua.maksym.hlushchenko.dao.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.role.AbstractUser;
import ua.maksym.hlushchenko.dao.entity.role.Admin;
import ua.maksym.hlushchenko.orm.dao.GenericDao;
import ua.maksym.hlushchenko.orm.dao.SessionImpl;
import ua.maksym.hlushchenko.orm.entity.EntityParser;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static ua.maksym.hlushchenko.dao.db.sql.TestEntityFactory.createEntity;

public class AdminGenericDaoTest {
    private static SessionImpl session;
    private static Dao<Integer, Admin> dao;

    @BeforeAll
    static void init() throws SQLException {
        session = new SessionImpl(HikariCPDataSource.getInstance().getConnection());
        dao = new GenericDao<>(Admin.class, session);
    }

    @BeforeEach
    @AfterEach
    void clearTable() {
        try (Connection connection = HikariCPDataSource.getInstance().getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            statement.executeUpdate("DELETE FROM " + EntityParser.getTableNameOf(AbstractUser.class));
            statement.executeUpdate("DELETE FROM " + EntityParser.getTableNameOf(Admin.class));
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void find()  {
        Admin admin = createEntity(Admin.class);

        dao.save(admin);
        session.commit();

        assertTrue(admin.getId() != 0);

        Optional<Admin> fromDb = dao.find(admin.getId());
        assertTrue(fromDb.isPresent());
        assertEquals(admin, fromDb.get());
    }

    @Test
    void findAll() {
        Admin admin1 = createEntity(Admin.class);
        Admin admin2 = createEntity(Admin.class);

        dao.save(admin1);
        dao.save(admin2);
        session.commit();

        List<Admin> fromDb = dao.findAll();
        assertTrue(fromDb.contains(admin1));
        assertTrue(fromDb.contains(admin2));
    }

    @Test
    void save() {
        Admin admin = createEntity(Admin.class);

        dao.save(admin);
        session.commit();

        assertTrue(admin.getId() != 0);
    }

    @Test
    void update() {
        Admin admin = createEntity(Admin.class);

        dao.save(admin);
        session.commit();

        admin = dao.find(admin.getId()).get();
        String newPassword = "0987654321";
        admin.setPasswordHash(newPassword);
        dao.update(admin);
        session.commit();

        admin = dao.find(admin.getId()).get();
        assertEquals(newPassword, admin.getPasswordHash());
    }

    @Test
    void delete() {
        Admin admin = createEntity(Admin.class);

        dao.save(admin);
        dao.delete(admin.getId());
        session.commit();

        Optional<Admin> fromDb = dao.find(admin.getId());
        assertTrue(fromDb.isEmpty());
    }

    @AfterAll
    static void destroy() {
        session.closeSession();
    }
}
