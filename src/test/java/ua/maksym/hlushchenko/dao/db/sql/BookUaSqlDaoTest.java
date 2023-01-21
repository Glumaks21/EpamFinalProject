package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.impl.*;
import ua.maksym.hlushchenko.dao.entity.impl.Genre;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookUaSqlDaoTest {
    private static Connection connection;
    private static BookUaSqlDao dao;
    private static Book book;

    static Book createBook() {
        Book book = new Book();
        book.setTitle("Тест");
        book.setAuthor(AuthorUaSqlDaoTest.createAuthor());
        book.setPublisher(PublisherSqlDaoTest.createPublisher());
        book.setDescription("Дуже великий тест");
        book.setDate(LocalDate.of(1111, 11, 11));
        return book;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        SqlDaoTestHelper.clearTables();
        connection = HikariCPDataSource.getInstance().getConnection();
        dao = new BookUaSqlDao(connection);
        book = createBook();

        Book original = BookEnSqlDaoTest.createBook();

        BookEnSqlDao bookSqlDao = new BookEnSqlDao(connection);
        bookSqlDao.save(original);

        Author authorUa = AuthorUaSqlDaoTest.createAuthor();
        authorUa.setId(original.getAuthor().getId());
        AuthorUaSqlDao authorSqlDao = new AuthorUaSqlDao(connection);
        authorSqlDao.save(authorUa);

        book.setId(original.getId());
        book.getAuthor().setId(original.getAuthor().getId());
        book.setPublisher(original.getPublisher());
    }

    @Order(1)
    @Test2
    void save() {
        dao.save(book);
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
        book.setTitle("Не тест");
        dao.update(book);
        find();
    }

    @Order(5)
    @Test2
    void saveGenres() {
        GenreEnSqlDao enGenreDao = new GenreEnSqlDao(connection);
        GenreUaSqlDao uaGenreDao = new GenreUaSqlDao(connection);

        List<Genre> genres = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            Genre genreEn = new Genre();
            Genre genreUa = new Genre();
            genreEn.setName("Genre " + i);
            genreUa.setName("Жанр " + i);
            enGenreDao.save(genreEn);
            genreUa.setId(genreEn.getId());
            uaGenreDao.save(genreUa);
            genres.add(genreUa);
        }
        book.setGenres(genres);

        dao.saveGenres(book);
    }

    @Order(6)
    @Test2
    void findGenres() {
        List<Genre> genres = dao.findGenres(book.getId());
        assertEquals(book.getGenres(), genres);
    }

    @Order(7)
    @Test2
    void updateGenres() {
        List<Genre> genres = book.getGenres();
        genres.remove(0);
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
