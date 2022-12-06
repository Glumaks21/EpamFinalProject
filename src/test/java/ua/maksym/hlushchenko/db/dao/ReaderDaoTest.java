package ua.maksym.hlushchenko.db.dao;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.db.HikariCPDataSource;
import ua.maksym.hlushchenko.db.entity.roles.Librarian;
import ua.maksym.hlushchenko.db.entity.roles.Reader;
import ua.maksym.hlushchenko.db.entity.roles.User;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReaderDaoTest {
    private static Connection connection;
    private static ReaderDao dao;
    private static Reader reader;

    static Reader createReader() {
        Reader reader = new Reader();
        reader.setBlocked(false);
        reader.setUser(UserDaoTest.createUser());
        return reader;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        connection = HikariCPDataSource.getConnection();
        dao = new ReaderDao(connection);
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
        Reader readerInDb = dao.find(reader.getUser().getLogin()).get();
        Assertions.assertEquals(reader, readerInDb);
    }

    @Order(4)
    @Test
    void update() {
        reader.setBlocked(true);
        dao.update(reader);
        Reader readerInDb = dao.find(reader.getUser().getLogin()).get();
        Assertions.assertEquals(reader, readerInDb);
    }

    @Order(5)
    @Test
    void delete() {
        dao.delete(reader.getUser().getLogin());
        Optional<Reader> librarianInDb = dao.find(reader.getUser().getLogin());
        Assertions.assertTrue(librarianInDb.isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        connection.close();
    }
}