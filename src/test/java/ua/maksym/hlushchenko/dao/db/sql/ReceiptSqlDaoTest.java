package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.Receipt;
import ua.maksym.hlushchenko.dao.entity.impl.ReceiptImpl;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReceiptSqlDaoTest {
    private static ReceiptSqlDao dao;
    private static ReaderSqlDao readerSqlDao;

    private static ReceiptImpl receipt;


    static ReceiptImpl createReceipt() {
        ReceiptImpl receipt = new ReceiptImpl();
        receipt.setReader(ReaderSqlDaoTest.createReader());
        receipt.setDateTime(LocalDateTime.of(1111, 11, 11, 11, 11, 11));
        return receipt;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        Connection connection = HikariCPDataSource.getConnection();
        dao = new ReceiptSqlDao(connection);
        receipt = createReceipt();

        readerSqlDao = new ReaderSqlDao(connection);
        readerSqlDao.save(receipt.getReader());
    }

    @Order(1)
    @Test
    void save() {
        dao.save(receipt);
        assertTrue(receipt.getId() != 0);
    }

    @Order(2)
    @Test
    void findAll() {
        List<Receipt> receipts = dao.findAll();
        assertTrue(receipts.contains(receipt));
    }

    @Order(3)
    @Test
    void find() {
        Optional<Receipt> optionalReceiptInDb = dao.find(receipt.getId());
        assertTrue(optionalReceiptInDb.isPresent());
        Receipt receiptInDb = optionalReceiptInDb.get();
        assertEquals(receipt, receiptInDb);
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
        assertTrue(dao.find(receipt.getId()).isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        readerSqlDao.delete(receipt.getReader().getLogin());

        readerSqlDao.close();
        dao.close();
    }
}