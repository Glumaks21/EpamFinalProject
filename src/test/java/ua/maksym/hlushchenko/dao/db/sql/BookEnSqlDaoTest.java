package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.impl.*;
import ua.maksym.hlushchenko.dao.entity.impl.Genre;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookEnSqlDaoTest {
    private static Connection connection;
    private static BookEnSqlDao dao;
    private static Book book;

    static Book createBook() {
        Book book = new Book();
        book.setTitle("test");
        book.setAuthor(AuthorEnSqlDaoTest.createAuthor());
        book.setPublisher(PublisherSqlDaoTest.createPublisher());
        book.setDescription("very big test");
        book.setDate(LocalDate.of(1111, 11, 11));
        return book;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        SqlDaoTestHelper.clearTables();
        connection = HikariCPDataSource.getInstance().getConnection();
        dao = new BookEnSqlDao(connection);
        book = createBook();
    }

    @Order(1)
    @Test2
    void save() {
        dao.save(book);
        assertTrue(book.getId() != 0);
    }

    @Order(2)
    @Test2
    void findAll() {
        List<Book> books = dao.findAll();
        assertTrue(books.contains(book));
    }

    @Order(3)
    @Test2
    void find() {
        Optional<Book> optionalBookInDb = dao.find(book.getId());
        assertTrue(optionalBookInDb.isPresent());
        Book bookInDb = optionalBookInDb.get();
        assertEquals(book, bookInDb);
    }

    @Order(4)
    @Test2
    void update() {
        book.setTitle("ne_test");
        book.setDate(LocalDate.now());
        dao.update(book);
        find();
    }

    @Order(5)
    @Test2
    void saveGenres() {
        Genre genre1 = new Genre();
        genre1.setName("Genre1");
        Genre genre2 = new Genre();
        genre2.setName("Genre2");

        List<ua.maksym.hlushchenko.dao.entity.Genre> genres = new ArrayList<>();
        genres.add(genre1);
        genres.add(genre2);
        book.setGenres(genres);

        dao.saveGenres(book);
    }

    @Order(6)
    @Test2
    void findGenres() {
        List<ua.maksym.hlushchenko.dao.entity.Genre> genres = dao.findGenres(book.getId());
        assertEquals(book.getGenres(), genres);
    }

    @Order(7)
    @Test2
    void updateGenres() {
        Genre genre3 = new Genre();
        genre3.setName("Genre3");

        List<ua.maksym.hlushchenko.dao.entity.Genre> genres = book.getGenres();
        genres.add(genre3);
        book.setGenres(genres);

        dao.updateGenres(book);

        findGenres();
    }

    @Order(8)
    @Test2
    void deleteGenres() {
        dao.deleteGenres(book.getId());
        assertTrue(dao.findGenres(book.getId()).isEmpty());
    }

    @Order(9)
    @Test2
    void delete() {
        dao.delete(book.getId());
        assertTrue(dao.find(book.getId()).isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        SqlDaoTestHelper.clearTables();
        connection.close();
    }
}