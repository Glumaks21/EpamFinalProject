package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.impl.role.LibrarianImpl;
import ua.maksym.hlushchenko.dao.entity.role.Librarian;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LibrarianSqlDaoTest {
    private static LibrarianSqlDao dao;
    private static Librarian librarian;

    static Librarian createLibrarian() {
        LibrarianImpl librarian = new LibrarianImpl();
        librarian.setLogin("test");
        librarian.setPassword("test");
        return librarian;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        dao = new LibrarianSqlDao(HikariCPDataSource.getInstance());
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
        Optional<Librarian> optionalLibrarianInDb = dao.find(librarian.getLogin());
        Assertions.assertTrue(optionalLibrarianInDb.isPresent());
        Librarian librarianInDb = optionalLibrarianInDb.get();
        Assertions.assertEquals(librarian, librarianInDb);
    }

    @Order(4)
    @Test
    void delete() {
        dao.delete(librarian.getLogin());
        Optional<Librarian> librarianInDb = dao.find(librarian.getLogin());
        Assertions.assertTrue(librarianInDb.isEmpty());
    }
}