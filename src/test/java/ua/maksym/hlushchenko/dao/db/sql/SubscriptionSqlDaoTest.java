package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.Book;
import ua.maksym.hlushchenko.dao.entity.Subscription;
import ua.maksym.hlushchenko.dao.entity.impl.SubscriptionImpl;
import ua.maksym.hlushchenko.dao.entity.role.Reader;
import ua.maksym.hlushchenko.dao.entity.role.Role;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SubscriptionSqlDaoTest {
    private static SqlDaoFactory sqlDaoFactory;
    private static SubscriptionSqlDao dao;
    private static Subscription subscription;

    static Subscription createSubscription() {
        Subscription subscription = new SubscriptionImpl();
        subscription.setReader(ReaderSqlDaoTest.createReader());
        subscription.setBook(BookEnSqlDaoTest.createBook());
        subscription.setTakenDate(LocalDate.of(1111, 11, 11));
        subscription.setBroughtDate(LocalDate.of(1112, 12, 12));
        subscription.setFine(777.0);
        return subscription;
    }

    @BeforeAll
    static void init() {
        SqlDaoTestHelper.clearTables();

        sqlDaoFactory = new SqlDaoFactory();
        RoleSqlDao roleSqlDao = sqlDaoFactory.createRoleDao();
        Role role = RoleSqlDaoTest.createRole();
        roleSqlDao.save(role);

        ReaderSqlDao readerSqlDao = sqlDaoFactory.createReaderDao();
        Reader reader = ReaderSqlDaoTest.createReader();
        reader.setRole(role);
        readerSqlDao.save(reader);

        BookSqlDao bookSqlDao = sqlDaoFactory.createBookDao(Locale.ENGLISH);
        Book book = BookEnSqlDaoTest.createBook();
        bookSqlDao.save(book);

        subscription = createSubscription();
        subscription.setReader(reader);
        subscription.setBook(book);
        dao = sqlDaoFactory.createSubscriptionDao();
    }

    @Order(1)
    @Test
    void save() {
        dao.save(subscription);
        assertTrue(subscription.getId() != 0);
    }

    @Order(2)
    @Test
    void findAll() {
        List<Subscription> subscriptions = dao.findAll();
        assertTrue(subscriptions.contains(subscription));
    }

    @Order(3)
    @Test
    void find() {
        Optional<Subscription> optionalSubscriptionInDb = dao.find(subscription.getId());
        assertTrue(optionalSubscriptionInDb.isPresent());
        Subscription subscriptionInDb = optionalSubscriptionInDb.get();
        assertEquals(subscription, subscriptionInDb);
    }

    @Order(4)
    @Test
    void update() {
        subscription.setTakenDate(LocalDate.now());
        subscription.setBroughtDate(LocalDate.now());
        subscription.setFine(1234);
        dao.update(subscription);
        find();
    }

    @Order(5)
    @Test
    void findByReaderId() {
        List<Subscription> subscriptions = dao.findByReaderId(subscription.getReader().getId());
        assertTrue(subscriptions.contains(subscription));
    }

    @Order(6)
    @Test
    void deleteByReaderId() {
        dao.deleteByReaderId(subscription.getReader().getId());
        List<Subscription> subscriptions = dao.findByReaderId(subscription.getReader().getId());
        assertTrue(subscriptions.isEmpty());
    }


    @Order(7)
    @Test
    void delete() {
        dao.save(subscription);
        dao.delete(subscription.getId());
        assertTrue(dao.find(subscription.getId()).isEmpty());
    }

    @AfterAll
    static void destroy() {
        SqlDaoTestHelper.clearTables();
        sqlDaoFactory.close();
    }
}