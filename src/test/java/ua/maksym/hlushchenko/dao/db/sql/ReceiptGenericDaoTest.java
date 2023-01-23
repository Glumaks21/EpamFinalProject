package ua.maksym.hlushchenko.dao.db.sql;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.impl.Receipt;
import ua.maksym.hlushchenko.dao.entity.impl.role.Reader;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import static ua.maksym.hlushchenko.dao.db.sql.TestEntityFactory.*;
import static org.junit.jupiter.api.Assertions.*;


public class ReceiptGenericDaoTest {
    private static Session session;
    private static Dao<Integer, Receipt> dao;

    @BeforeAll
    static void init() throws SQLException {
        session = new Session(HikariCPDataSource.getInstance().getConnection());
        dao = new GenericDao<>(Receipt.class, session);
    }

    @BeforeEach
    @AfterEach
    void clearTable() {
        try (Connection connection = HikariCPDataSource.getInstance().getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            statement.executeUpdate("DELETE FROM " + EntityParser.getTableNameOf(Receipt.class));
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void find()  {
        TestEntityFactory testEntityFactory = new TestEntityFactory(session);
        Receipt receipt = createEntity(Receipt.class);
        testEntityFactory.setSavedValue(Reader.class, receipt);

        dao.save(receipt);
        session.commit();

        assertTrue(receipt.getId() != 0);

        Optional<Receipt> fromDb = dao.find(receipt.getId());
        assertTrue(fromDb.isPresent());
        assertEquals(receipt, fromDb.get());
    }

    @Test
    void findAll() {
        TestEntityFactory testEntityFactory = new TestEntityFactory(session);
        Receipt receipt1 = createEntity(Receipt.class);
        testEntityFactory.setSavedValue(Reader.class, receipt1);
        Receipt receipt2 = createEntity(Receipt.class);
        testEntityFactory.setSavedValue(Reader.class, receipt2);

        dao.save(receipt1);
        dao.save(receipt2);
        session.commit();

        List<Receipt> authorsIbDb = dao.findAll();
        assertTrue(authorsIbDb.contains(receipt1));
        assertTrue(authorsIbDb.contains(receipt2));
    }

    @Test
    void save() {
        TestEntityFactory testEntityFactory = new TestEntityFactory(session);
        Receipt receipt = createEntity(Receipt.class);
        testEntityFactory.setSavedValue(Reader.class, receipt);

        dao.save(receipt);
        session.commit();

        assertTrue(receipt.getId() != 0);
    }

    @Test
    void update() {
        TestEntityFactory testEntityFactory = new TestEntityFactory(session);
        Receipt receipt = createEntity(Receipt.class);
        testEntityFactory.setSavedValue(Reader.class, receipt);

        dao.save(receipt);
        session.commit();

        receipt = dao.find(receipt.getId()).get();
        LocalDateTime newDateTime = LocalDateTime.now().minusDays(3);
        receipt.setDateTime(newDateTime);
        dao.update(receipt);
        session.commit();

        receipt = dao.find(receipt.getId()).get();
        assertEquals(newDateTime, receipt.getDateTime());
    }

    @Test
    void delete() {
        TestEntityFactory testEntityFactory = new TestEntityFactory(session);
        Receipt receipt = createEntity(Receipt.class);
        testEntityFactory.setSavedValue(Reader.class, receipt);

        dao.save(receipt);
        dao.delete(receipt.getId());
        session.commit();

        Optional<Receipt> fromDb = dao.find(receipt.getId());
        assertTrue(fromDb.isEmpty());
    }

    @AfterAll
    static void destroy() {
        session.closeSession();
    }
}
