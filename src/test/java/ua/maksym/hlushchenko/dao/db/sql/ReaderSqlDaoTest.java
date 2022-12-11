package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.Receipt;
import ua.maksym.hlushchenko.dao.entity.impl.ReceiptImpl;
import ua.maksym.hlushchenko.dao.entity.impl.role.ReaderImpl;
import ua.maksym.hlushchenko.dao.entity.role.Reader;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReaderSqlDaoTest {
    private static Connection connection;
    private static ReaderSqlDao dao;
    private static ReaderImpl reader;

    static ReaderImpl createReader() {
        ReaderImpl reader = new ReaderImpl();

        reader.setBlocked(false);
        reader.setLogin("test");
        reader.setPassword("test");

        Receipt receipt = new ReceiptImpl();
        receipt.setReader(reader);
        receipt.setDateTime(LocalDateTime.of(1111, 11, 11, 11, 11, 11));

        List<Receipt> receipts = new ArrayList<>();


        return reader;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        connection = HikariCPDataSource.getConnection();
        dao = new ReaderSqlDao(connection);
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
        Assertions.assertTrue(readers.contains(reader));
    }

    @Order(3)
    @Test
    void find() {
        Optional<Reader> optionalReaderInDb = dao.find(reader.getLogin());
        Assertions.assertTrue(optionalReaderInDb.isPresent());
        Reader readerInDb = optionalReaderInDb.get();
        Assertions.assertEquals(reader, readerInDb);
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
    void delete() {
        dao.delete(reader.getLogin());
        Optional<Reader> librarianInDb = dao.find(reader.getLogin());
        Assertions.assertTrue(librarianInDb.isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        connection.close();
    }
}