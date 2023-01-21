package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.impl.Subscription;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SubscriptionSqlDaoTest {
    private static Connection connection;
    private static SubscriptionSqlDao dao;
    private static Subscription subscription;

    static Subscription createSubscription() {
        Subscription subscription = new Subscription();
        subscription.setReader(ReaderSqlDaoTest.createReader());
        subscription.setBook(BookEnSqlDaoTest.createBook());
        subscription.setTakenDate(LocalDate.of(1111, 11, 11));
        subscription.setBroughtDate(LocalDate.of(1112, 12, 12));
        subscription.setFine(777.0);
        return subscription;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        SqlDaoTestHelper.clearTables();
        connection = HikariCPDataSource.getInstance().getConnection();

        ReaderSqlDao readerSqlDao = new ReaderSqlDao(connection);
        Reader reader = ReaderSqlDaoTest.createReader();
        readerSqlDao.save(reader);

        BookEnSqlDao bookSqlDao = new BookEnSqlDao(connection);
        Book book = BookEnSqlDaoTest.createBook();
        bookSqlDao.save(book);

        subscription = createSubscription();
        subscription.setReader(reader);
        subscription.setBook(book);
        dao = new SubscriptionSqlDao(connection);
    }

    @Order(1)
    @Test2
    void save() {
        dao.save(subscription);
        assertTrue(subscription.getId() != 0);
    }

    @Order(2)
    @Test2
    void findAll() {
        List<Subscription> subscriptions = dao.findAll();
        assertTrue(subscriptions.contains(subscription));
    }

    @Order(3)
    @Test2
    void find() {
        Optional<Subscription> optionalSubscriptionInDb = dao.find(subscription.getId());
        assertTrue(optionalSubscriptionInDb.isPresent());
        Subscription subscriptionInDb = optionalSubscriptionInDb.get();
        assertEquals(subscription, subscriptionInDb);
    }

    @Order(4)
    @Test2
    void update() {
        subscription.setTakenDate(LocalDate.now());
        subscription.setBroughtDate(LocalDate.now());
        subscription.setFine(1234);
        dao.update(subscription);
        find();
    }

    @Order(5)
    @Test2
    void findByReaderId() {
        List<Subscription> subscriptions = dao.findByReaderId(subscription.getReader().getId());
        assertTrue(subscriptions.contains(subscription));
    }

    @Order(6)
    @Test2
    void deleteByReaderId() {
        dao.deleteByReaderId(subscription.getReader().getId());
        List<Subscription> subscriptions = dao.findByReaderId(subscription.getReader().getId());
        assertTrue(subscriptions.isEmpty());
    }


    @Order(7)
    @Test2
    void delete() {
        dao.save(subscription);
        dao.delete(subscription.getId());
        assertTrue(dao.find(subscription.getId()).isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        SqlDaoTestHelper.clearTables();
        connection.close();
    }
}