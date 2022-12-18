package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.impl.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookEnSqlDaoTest {
    private static BookSqlDao dao;
    private static BookImpl book;

    private static AuthorSqlDao authorDao;
    private static PublisherSqlDao publisherDao;
    private static GenreSqlDao genreDao;

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
        setUpTables();

        SqlDaoFactory sqlDaoFactory = new SqlDaoFactory();
        dao = sqlDaoFactory.createBookDao(Locale.ENGLISH);
        authorDao = sqlDaoFactory.createAuthorDao(Locale.ENGLISH);
        publisherDao = sqlDaoFactory.createPublisherDao();
        genreDao = sqlDaoFactory.createGenreDao(Locale.ENGLISH);

        book = createBook();

        authorDao.save(book.getAuthor());
        publisherDao.save(book.getPublisher());
    }

    @SneakyThrows
    static void setUpTables() {
        AuthorEnSqlDaoTest.setUpTables();
        PublisherSqlDaoTest.setUpTables();
        GenreEnSqlDaoTest.setUpTables();

        String dropCoverQuery = "DROP TABLE IF EXISTS `cover`";
        String createCoverQuery = "CREATE TABLE `cover` (\n" +
                "                         `id` int NOT NULL AUTO_INCREMENT,\n" +
                "                         `img` blob NOT NULL,\n" +
                "                         PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci";

        String dropBookQuery = "DROP TABLE IF EXISTS `book`";
        String createBookQuery = "CREATE TABLE `book` (\n" +
                "                        `id` int NOT NULL AUTO_INCREMENT,\n" +
                "                        `title` varchar(45) NOT NULL,\n" +
                "                        `author_id` int NOT NULL,\n" +
                "                        `publisher_isbn` varchar(17) NOT NULL,\n" +
                "                        `date` date DEFAULT NULL,\n" +
                "                        `description` varchar(500) NOT NULL DEFAULT 'No info',\n" +
                "                        `cover_id` int DEFAULT NULL,\n" +
                "                        PRIMARY KEY (`id`),\n" +
                "                        KEY `fk_book_author_idx` (`author_id`),\n" +
                "                        KEY `fk_book_publisher_isbn` (`publisher_isbn`),\n" +
                "                        KEY `fk_book_cover_id_idx` (`cover_id`),\n" +
                "                        CONSTRAINT `fk_book_author` FOREIGN KEY (`author_id`) REFERENCES `author` (`id`),\n" +
                "                        CONSTRAINT `fk_book_cover_id` FOREIGN KEY (`cover_id`) REFERENCES `cover` (`id`),\n" +
                "                        CONSTRAINT `fk_book_publisher_isbn` FOREIGN KEY (`publisher_isbn`) REFERENCES `publisher` (`isbn`)\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=229 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci";

        String dropBooksGenresQuery = "DROP TABLE IF EXISTS `book_has_genre`";
        String createBooksGenresQuery = "CREATE TABLE `book_has_genre` (\n" +
                "                                  `book_id` int NOT NULL,\n" +
                "                                  `genre_id` int NOT NULL,\n" +
                "                                  PRIMARY KEY (`book_id`,`genre_id`),\n" +
                "                                  KEY `fk_book_has_genre_genre1_idx` (`genre_id`),\n" +
                "                                  KEY `fk_book_has_genre_book1_idx` (`book_id`),\n" +
                "                                  CONSTRAINT `fk_book_has_genre_book1` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`),\n" +
                "                                  CONSTRAINT `fk_book_has_genre_genre1` FOREIGN KEY (`genre_id`) REFERENCES `genre` (`id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci";

        Connection connection = HikariCPDataSource.getInstance().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropCoverQuery);
        statement.executeUpdate(createCoverQuery);
        statement.executeUpdate(dropBookQuery);
        statement.executeUpdate(createBookQuery);
        statement.executeUpdate(dropBooksGenresQuery);
        statement.executeUpdate(createBooksGenresQuery);
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

        book.setGenres(List.of(genre1, genre2));

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
        dropTables();
    }

    @SneakyThrows
    static void dropTables() {
        String dropBooksGenresQuery = "DROP TABLE book_has_genre";
        String dropBookQuery = "DROP TABLE `book`";
        String dropCoverQuery = "DROP TABLE `cover`";

        Connection connection = HikariCPDataSource.getInstance().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropBooksGenresQuery);
        statement.executeUpdate(dropBookQuery);
        statement.executeUpdate(dropCoverQuery);

        AuthorEnSqlDaoTest.dropTables();
        PublisherSqlDaoTest.dropTables();
        GenreEnSqlDaoTest.dropTables();
    }
}