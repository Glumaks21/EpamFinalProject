package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.Receipt;
import ua.maksym.hlushchenko.dao.entity.impl.ReceiptImpl;
import ua.maksym.hlushchenko.dao.entity.role.Reader;
import ua.maksym.hlushchenko.dao.entity.role.Role;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReceiptSqlDaoTest {
    private static SqlDaoFactory sqlDaoFactory;
    private static ReceiptSqlDao dao;
    private static Receipt receipt;

    static Receipt createReceipt() {
        ReceiptImpl receipt = new ReceiptImpl();
        receipt.setReader(ReaderSqlDaoTest.createReader());
        receipt.setDateTime(LocalDateTime.of(1111, 11, 11, 11, 11, 11));
        return receipt;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        sqlDaoFactory = new SqlDaoFactory();
        dao = sqlDaoFactory.createReceiptDao();
        receipt = createReceipt();

        RoleSqlDao roleSqlDao = sqlDaoFactory.createRoleDao();
        Role role = RoleSqlDaoTest.createRole();
        roleSqlDao.save(role);
        Role savedRole = roleSqlDao.find(role.getId()).get();

        ReaderSqlDao readerSqlDao = sqlDaoFactory.createReaderDao();
        Reader reader = ReaderSqlDaoTest.createReader();
        reader.setRole(savedRole);
        readerSqlDao.save(reader);
        Reader savedReader = readerSqlDao.find(reader.getId()).get();
        receipt.setReader(savedReader);
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
        sqlDaoFactory.close();
    }
}