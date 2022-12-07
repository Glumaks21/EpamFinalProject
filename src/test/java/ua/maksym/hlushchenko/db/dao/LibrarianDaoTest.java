package ua.maksym.hlushchenko.db.dao;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.db.HikariCPDataSource;
import ua.maksym.hlushchenko.db.dao.sql.LibrarianSqlDao;
import ua.maksym.hlushchenko.db.entity.roles.Librarian;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LibrarianDaoTest {
    private static Connection connection;
    private static LibrarianSqlDao dao;
    private static Librarian librarian;

    static Librarian createLibrarian() {
        Librarian librarian = new Librarian();
        librarian.setUser(UserDaoTest.createUser());
        return librarian;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        connection = HikariCPDataSource.getConnection();
        dao = new LibrarianSqlDao(connection);
        librarian = createLibrarian();
    }

    @Order(1)
    @Test
    void save() {
        dao.save(librarian);
    }

    @SneakyThrows
    @Order(2)
    @Test
    void findAll() {
        List<Librarian> librarians = dao.findAll();
        Assertions.assertTrue(librarians.contains(librarian));
    }

    @Order(3)
    @Test
    void find() {
        Optional<Librarian> optionalLibrarianInDb = dao.find(librarian.getUser().getLogin());
        Assertions.assertTrue(optionalLibrarianInDb.isPresent());
        Librarian librarianInDb = optionalLibrarianInDb.get();
        Assertions.assertEquals(librarian, librarianInDb);
    }

    @Order(4)
    @Test
    void delete() {
        dao.delete(librarian.getUser().getLogin());
        Optional<Librarian> librarianInDb = dao.find(librarian.getUser().getLogin());
        Assertions.assertTrue(librarianInDb.isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        connection.close();
    }
}