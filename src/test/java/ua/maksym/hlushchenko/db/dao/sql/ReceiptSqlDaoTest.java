package ua.maksym.hlushchenko.db.dao.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.db.HikariCPDataSource;
import ua.maksym.hlushchenko.db.entity.Receipt;
import ua.maksym.hlushchenko.db.entity.model.ReceiptModel;
import ua.maksym.hlushchenko.db.entity.model.role.ReaderModel;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReceiptSqlDaoTest {
    private static ReceiptSqlDao dao;
    private static ReceiptModel receipt;

    private static ReaderSqlDao readerDao;

    static ReceiptModel createReceipt() {
        ReceiptModel receipt = new ReceiptModel();
        ReaderModel reader = ReaderSqlDaoTest.createReader();
        receipt.setReader(reader);
        receipt.setDateTime(LocalDateTime.of(1111, 11, 11, 11, 11, 11));
        return receipt;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        Connection connection = HikariCPDataSource.getConnection();
        dao = new ReceiptSqlDao(connection);
        receipt = createReceipt();

        readerDao = new ReaderSqlDao(connection);
        readerDao.save(receipt.getReader());
    }

    @Order(1)
    @Test
    void save() {
        dao.save(receipt);
        Assertions.assertTrue(receipt.getId() != 0);
    }

    @Order(2)
    @Test
    void findAll() {
        List<Receipt> receipts = dao.findAll();
        Assertions.assertTrue(receipts.contains(receipt));
    }

    @Order(3)
    @Test
    void find() {
        Optional<Receipt> optionalReceiptInDb = dao.find(receipt.getId());
        Assertions.assertTrue(optionalReceiptInDb.isPresent());
        Receipt receiptInDb = optionalReceiptInDb.get();
        Assertions.assertEquals(receipt, receiptInDb);
    }

    @Order(4)
    @Test
    void update() {
        receipt.setDateTime(LocalDateTime.now());
        dao.update(receipt);
        find();
    }

    @Order(5)
    @Test
    void delete() {
        dao.delete(receipt.getId());
        Assertions.assertTrue(dao.find(receipt.getId()).isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        readerDao.delete(receipt.getReader().getLogin());

        readerDao.close();
        dao.close();
    }
}