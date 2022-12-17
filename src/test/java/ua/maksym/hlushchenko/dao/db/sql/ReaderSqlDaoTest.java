package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;

import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.impl.role.ReaderImpl;
import ua.maksym.hlushchenko.dao.entity.role.Reader;
import ua.maksym.hlushchenko.util.Sha256Encoder;

import javax.sql.DataSource;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReaderSqlDaoTest {
    private static ReaderSqlDao dao;
    private static ReceiptSqlDao receiptSqlDao;
    private static SubscriptionSqlDao subscriptionSqlDao;
    private static BookEnSqlDao bookSqlDao;
    private static AuthorEnSqlDao authorSqlDao;
    private static PublisherSqlDao publisherSqlDao;

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
        DataSource ds = HikariCPDataSource.getInstance();
        dao = new ReaderSqlDao(ds);
        receiptSqlDao = new ReceiptSqlDao(ds);
        subscriptionSqlDao = new SubscriptionSqlDao(ds);
        authorSqlDao = new AuthorEnSqlDao(ds);
        publisherSqlDao = new PublisherSqlDao(ds);
        bookSqlDao = new BookEnSqlDao(ds);

        reader = createReader();
    }

    @Order(1)
    @Test
    void save() {
        dao.save(reader);
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
        receipt1.setReader(reader);
        receipt2.setReader(reader);
        receiptSqlDao.save(receipt1);
        receiptSqlDao.save(receipt2);
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
        receipt3.setReader(reader);
        receiptSqlDao.save(receipt3);
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
        reader.getReceipts().forEach(
                receipt -> receiptSqlDao.delete(receipt.getId())
        );
    }

    @Order(9)
    @Test
    void saveSubscriptions() {
        Subscription subscription1 = SubscriptionSqlDaoTest.createSubscription();
        Subscription subscription2 = SubscriptionSqlDaoTest.createSubscription();
        Book commonBook = subscription1.getBook();
        Author commonAuthor = commonBook.getAuthor();
        Publisher commonPublisher = commonBook.getPublisher();

        subscription1.setReader(reader);
        subscription2.setReader(reader);

        subscription2.setBook(commonBook);

        authorSqlDao.save(commonAuthor);
        publisherSqlDao.save(commonPublisher);
        bookSqlDao.save(commonBook);

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
        subscription3.setReader(reader);
        subscription3.setBook(subscriptions.get(0).getBook());
        subscriptionSqlDao.save(subscription3);
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
        bookSqlDao.delete(reader.getSubscriptions().
                get(0).getBook().getId());
        authorSqlDao.delete(reader.getSubscriptions().
                get(0).getBook().getAuthor().getId());
        publisherSqlDao.delete(reader.getSubscriptions().
                get(0).getBook().getPublisher().getIsbn());

    }
}