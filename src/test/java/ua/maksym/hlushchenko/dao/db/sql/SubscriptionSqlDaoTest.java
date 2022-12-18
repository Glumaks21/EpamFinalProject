package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.Subscription;
import ua.maksym.hlushchenko.dao.entity.impl.SubscriptionImpl;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SubscriptionSqlDaoTest {
    private static SqlDaoFactory sqlDaoFactory;
    private static SubscriptionSqlDao dao;
    private static ReaderSqlDao readerSqlDao;
    private static BookSqlDao bookSqlDao;
    private static AuthorSqlDao authorSqlDao;
    private static PublisherSqlDao publisherSqlDao;

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

    @SneakyThrows
    @BeforeAll
    static void init() {
        sqlDaoFactory = new SqlDaoFactory();
        dao = sqlDaoFactory.createSubscriptionDao();
        readerSqlDao = sqlDaoFactory.createReaderDao();
        bookSqlDao = sqlDaoFactory.createBookDao(Locale.ENGLISH);
        authorSqlDao = sqlDaoFactory.createAuthorDao(Locale.ENGLISH);
        publisherSqlDao = sqlDaoFactory.createPublisherDao();

        subscription = createSubscription();
        readerSqlDao.save(subscription.getReader());
        authorSqlDao.save(subscription.getBook().getAuthor());
        publisherSqlDao.save(subscription.getBook().getPublisher());
        bookSqlDao.save(subscription.getBook());
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
    void delete() {
        dao.delete(subscription.getId());
        assertTrue(dao.find(subscription.getId()).isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        sqlDaoFactory.close();
    }
}