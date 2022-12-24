package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;

import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.impl.role.ReaderImpl;
import ua.maksym.hlushchenko.dao.entity.role.*;
import ua.maksym.hlushchenko.util.Sha256Encoder;

import java.sql.Connection;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReaderSqlDaoTest {
    private static Connection connection;
    private static ReaderSqlDao dao;
    private static Reader reader;

    static Reader createReader() {
        ReaderImpl reader = new ReaderImpl();
        reader.setLogin("test");
        reader.setPasswordHash(Sha256Encoder.encode("test"));
        reader.setBlocked(false);
        return reader;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        SqlDaoTestHelper.clearTables();
        connection = HikariCPDataSource.getInstance().getConnection();
        dao = new ReaderSqlDao(connection);
        reader = createReader();
    }

    @Order(1)
    @Test
    void save() {
        dao.save(reader);
        assertTrue(reader.getId() != 0);
    }

    @Order(2)
    @Test
    void findAll() {
        List<Reader> readers = dao.findAll();
        assertTrue(readers.contains(reader));
    }

    @Order(3)
    @Test
    void find() {
        Optional<Reader> optionalReaderInDb = dao.find(reader.getId());
        assertTrue(optionalReaderInDb.isPresent());
        Reader readerInDb = optionalReaderInDb.get();
        assertEquals(reader, readerInDb);
    }

    @Order(4)
    @Test
    void update() {
        reader.setBlocked(true);
        dao.update(reader);
        find();
    }

    @Order(5)
    @Test
    void saveReceipts() {
        Receipt receipt1 = ReceiptSqlDaoTest.createReceipt();
        Receipt receipt2 = ReceiptSqlDaoTest.createReceipt();
        List<Receipt> receipts = new ArrayList<>();
        receipts.add(receipt1);
        receipts.add(receipt2);
        reader.setReceipts(receipts);

        dao.saveReceipts(reader);
    }

    @Order(6)
    @Test
    void findReceipts() {
        List<Receipt> receipts = dao.findReceipts(reader.getId());
        assertEquals(reader.getReceipts(), receipts);
    }

    @Order(7)
    @Test
    void updateReceipts() {
        Receipt receipt3 = ReceiptSqlDaoTest.createReceipt();
        List<Receipt> receipts = reader.getReceipts();
        receipts.add(receipt3);
        reader.setReceipts(receipts);

        dao.updateReceipts(reader);
        findReceipts();
    }

    @Order(8)
    @Test
    void deleteReceipts() {
        dao.deleteReceipts(reader.getId());
        assertTrue(dao.findReceipts(reader.getId()).isEmpty());
    }

    @Order(9)
    @Test
    void saveSubscriptions() {
        Book book = BookEnSqlDaoTest.createBook();
        BookEnSqlDao bookSqlDao = new BookEnSqlDao(connection);
        bookSqlDao.save(book);
        Book savedBook = bookSqlDao.find(book.getId()).get();

        Subscription subscription1 = SubscriptionSqlDaoTest.createSubscription();
        Subscription subscription2 = SubscriptionSqlDaoTest.createSubscription();
        subscription1.setBook(savedBook);
        subscription2.setBook(savedBook);

        List<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(subscription1);
        subscriptions.add(subscription2);
        reader.setSubscriptions(subscriptions);

        dao.saveSubscriptions(reader);
    }

    @Order(10)
    @Test
    void findSubscriptions() {
        List<Subscription> subscriptions = dao.findSubscriptions(reader.getId());
        assertEquals(reader.getSubscriptions(), subscriptions);
    }

    @Order(11)
    @Test
    void updateSubscriptions() {
        List<Subscription> subscriptions = reader.getSubscriptions();
        Subscription subscription3 = SubscriptionSqlDaoTest.createSubscription();
        subscription3.setBook(subscriptions.get(0).getBook());
        subscriptions.add(subscription3);
        reader.setSubscriptions(subscriptions);

        dao.updateSubscriptions(reader);
        findSubscriptions();
    }

    @Order(12)
    @Test
    void deleteSubscriptions() {
        dao.deleteSubscriptions(reader.getId());
        assertTrue(dao.findSubscriptions(reader.getId()).isEmpty());
    }

    @Order(13)
    @Test
    void delete() {
        dao.delete(reader.getId());
        assertTrue(dao.find(reader.getId()).isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        SqlDaoTestHelper.clearTables();
        connection.close();
    }
}