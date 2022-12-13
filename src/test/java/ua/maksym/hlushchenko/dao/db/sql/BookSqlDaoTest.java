package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.impl.*;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookSqlDaoTest {
    private static BookSqlDao dao;
    private static BookImpl book;

    private static AuthorSqlDao authorDao;
    private static PublisherSqlDao publisherDao;
    private static GenreSqlDao genreDao;

    static BookImpl createBook() {
        BookImpl book = new BookImpl();
        book.setTitle("test");

        AuthorImpl author = AuthorSqlDaoTest.createAuthor();
        book.setAuthor(author);

        PublisherImpl publisher = PublisherSqlDaoTest.createPublisher();
        book.setPublisher(publisher);

        book.setDate(LocalDate.of(1111, 11, 11));
        return book;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        Connection connection = HikariCPDataSource.getConnection();
        dao = new BookSqlDao(connection);
        book = createBook();

        authorDao = new AuthorSqlDao(connection);
        publisherDao = new PublisherSqlDao(connection);
        genreDao = new GenreSqlDao(connection);

        authorDao.save(book.getAuthor());
        publisherDao.save(book.getPublisher());
        book.getGenres().forEach(genreDao::save);
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
        genreDao.save(genre1);
        genreDao.save(genre2);

        List<Genre> genres = new ArrayList<>();
        genres.add(genre1);
        genres.add(genre2);
        book.setGenres(genres);

        dao.saveGenres(book);
    }

    @Order(6)
    @Test
    void findGenres() {
        List<Genre> genres = dao.findGenres(book.getId());
        assertEquals(book.getGenres(), genres);
    }

    @Order(7)
    @Test
    void updateGenres() {
        GenreImpl genre3 = new GenreImpl();
        genre3.setName("Genre3");
        genreDao.save(genre3);

        List<Genre> genres = book.getGenres();
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
        authorDao.delete(book.getAuthor().getId());
        publisherDao.delete(book.getPublisher().getIsbn());
        book.getGenres().forEach(genre -> genreDao.delete(genre.getId()));

        authorDao.close();
        publisherDao.close();
        genreDao.close();
    }
}