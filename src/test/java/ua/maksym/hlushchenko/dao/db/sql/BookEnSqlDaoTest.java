package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.impl.*;
import ua.maksym.hlushchenko.dao.entity.impl.GenreImpl;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookEnSqlDaoTest {
    private static Connection connection;
    private static BookEnSqlDao dao;
    private static Book book;

    static BookImpl createBook() {
        BookImpl book = new BookImpl();
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
    @Test
    void save() {
        dao.save(book);
        assertTrue(book.getId() != 0);
    }

    @Order(2)
    @Test
    void findAll() {
        List<Book> books = dao.findAll();
        assertTrue(books.contains(book));
    }

    @Order(3)
    @Test
    void find() {
        Optional<Book> optionalBookInDb = dao.find(book.getId());
        assertTrue(optionalBookInDb.isPresent());
        Book bookInDb = optionalBookInDb.get();
        assertEquals(book, bookInDb);
    }

    @Order(4)
    @Test
    void update() {
        book.setTitle("ne_test");
        book.setDate(LocalDate.now());
        dao.update(book);
        find();
    }

    @Order(5)
    @Test
    void saveGenres() {
        GenreImpl genre1 = new GenreImpl();
        genre1.setName("Genre1");
        GenreImpl genre2 = new GenreImpl();
        genre2.setName("Genre2");

        List<ua.maksym.hlushchenko.dao.entity.Genre> genres = new ArrayList<>();
        genres.add(genre1);
        genres.add(genre2);
        book.setGenres(genres);

        dao.saveGenres(book);
    }

    @Order(6)
    @Test
    void findGenres() {
        List<ua.maksym.hlushchenko.dao.entity.Genre> genres = dao.findGenres(book.getId());
        assertEquals(book.getGenres(), genres);
    }

    @Order(7)
    @Test
    void updateGenres() {
        GenreImpl genre3 = new GenreImpl();
        genre3.setName("Genre3");

        List<ua.maksym.hlushchenko.dao.entity.Genre> genres = book.getGenres();
        genres.add(genre3);
        book.setGenres(genres);

        dao.updateGenres(book);

        findGenres();
    }

    @Order(8)
    @Test
    void deleteGenres() {
        dao.deleteGenres(book.getId());
        assertTrue(dao.findGenres(book.getId()).isEmpty());
    }

    @Order(9)
    @Test
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