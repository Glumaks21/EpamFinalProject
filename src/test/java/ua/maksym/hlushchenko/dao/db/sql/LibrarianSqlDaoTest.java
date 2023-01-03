package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.sql.role.LibrarianImpl;
import ua.maksym.hlushchenko.dao.entity.role.Librarian;
import ua.maksym.hlushchenko.util.Sha256Encoder;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LibrarianSqlDaoTest {
    private static Connection connection;
    private static LibrarianSqlDao dao;
    private static Librarian librarian;

    static Librarian createLibrarian() {
        Librarian librarian = new LibrarianImpl();
        librarian.setLogin("librarian");
        librarian.setPasswordHash(Sha256Encoder.encode("It is true"));
        return librarian;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        connection = HikariCPDataSource.getInstance().getConnection();
        dao = new LibrarianSqlDao(connection);
        librarian = createLibrarian();
    }

    @Order(1)
    @Test
    void save() {
        dao.save(librarian);
        assertTrue(librarian.getId() > 0);
    }

    @Order(2)
    @Test
    void findAll() {
        List<Librarian> librarians = dao.findAll();
        assertTrue(librarians.contains(librarian));
    }

    @Order(3)
    @Test
    void find() {
        Optional<Librarian> optionalLibrarian = dao.find(librarian.getId());
        assertTrue(optionalLibrarian.isPresent());
        Librarian librarianInDb = optionalLibrarian.get();
        assertEquals(librarian, librarianInDb);
    }

    @Order(4)
    @Test
    void update() {
        librarian.setPasswordHash(Sha256Encoder.encode("This is was joke"));
        dao.update(librarian);
        find();
    }

    @Order(5)
    @Test
    void delete() {
        dao.delete(librarian.getId());
        assertTrue(dao.find(librarian.getId()).isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        SqlDaoTestHelper.clearTables();
        connection.close();
    }
}