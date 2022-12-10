package ua.maksym.hlushchenko.db.dao.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.db.HikariCPDataSource;
import ua.maksym.hlushchenko.db.entity.*;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookSqlDaoTest {
    private static BookSqlDao dao;
    private static Book book;

    private static AuthorSqlDao authorDao;
    private static PublisherSqlDao publisherDao;
    private static GenreSqlDao genreDao;

    static Book createBook() {
        Book book = new Book();

        book.setTitle("test");

        Author author = AuthorSqlDaoTest.createAuthor();
        book.setAuthor(author);

        Publisher publisher = PublisherSqlDaoTest.createPublisher();
        book.setPublisher(publisher);

        book.setDate(LocalDate.of(1111, 11, 11));

        Genre genre1 = new Genre();
        genre1.setName("Genre1");
        Genre genre2 = new Genre();
        genre2.setName("Genre2");

        Set<Genre> genres = new HashSet<>();
        genres.add(genre1);
        genres.add(genre2);

        book.setGenres(genres);

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
        Assertions.assertTrue(book.getId() != 0);
    }

    @Order(2)
    @Test
    void findAll() {
        List<Book> books = dao.findAll();
        Assertions.assertTrue(books.contains(book));
    }

    @Order(3)
    @Test
    void find() {
        Optional<Book> optionalBookInDb = dao.find(book.getId());
        Assertions.assertTrue(optionalBookInDb.isPresent());
        Book bookInDb = optionalBookInDb.get();
        Assertions.assertEquals(book, bookInDb);
    }

    @Order(4)
    @Test
    void update() {
        book.setTitle("ne_test");
        book.setDate(LocalDate.now());

        Genre genre3 = new Genre();
        genre3.setName("Genre3");
        genreDao.save(genre3);

        Set<Genre> genres = book.getGenres();
        genres.add(genre3);
        book.setGenres(genres);

        dao.update(book);
        find();
    }

    @Order(5)
    @Test
    void delete() {
        dao.delete(book.getId());
        Assertions.assertTrue(dao.find(book.getId()).isEmpty());
        Assertions.assertTrue(dao.findGenres(book.getId()).isEmpty());
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