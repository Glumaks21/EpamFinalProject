package ua.maksym.hlushchenko.db.dao.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.db.HikariCPDataSource;
import ua.maksym.hlushchenko.db.entity.model.role.ReaderModel;
import ua.maksym.hlushchenko.db.entity.role.Reader;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReaderSqlDaoTest {
    private static Connection connection;
    private static ReaderSqlDao dao;
    private static ReaderModel reader;

    static ReaderModel createReader() {
        ReaderModel reader = new ReaderModel();
        reader.setBlocked(false);
        reader.setLogin("test");
        reader.setPassword("test");
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