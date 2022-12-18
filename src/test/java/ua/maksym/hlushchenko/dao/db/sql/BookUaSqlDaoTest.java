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
public class BookUaSqlDaoTest {
    private static BookSqlDao dao;
    private static BookSqlDao originalDao;
    private static Book book;
    private static Book originalBook;

    private static AuthorSqlDao authorDao;
    private static AuthorSqlDao authorOriginalDao;
    private static PublisherSqlDao publisherDao;
    private static GenreSqlDao genreDao;
    private static GenreSqlDao genreOriginalDao;

    static BookImpl createBook() {
        BookImpl book = new BookImpl();
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
        setUpTables();
        setUpDao();
        book = createBook();
        setUpOriginals();

        book.getAuthor().setId(originalBook.getAuthor().getId());
        book.setPublisher(originalBook.getPublisher());
        book.setId(originalBook.getId());

        publisherDao.save(book.getPublisher());
    }

    @SneakyThrows
    static void setUpTables() {
        AuthorUaSqlDaoTest.setUpTables();
        PublisherSqlDaoTest.setUpTables();
        GenreUaSqlDaoTest.setUpTables();
        setUpOriginalBookTables();

        String dropBookQuery = "DROP TABLE IF EXISTS `book_ua`";
        String createBookQuery = "CREATE TABLE `book_ua` (\n" +
                "                           `book_id` int NOT NULL AUTO_INCREMENT,\n" +
                "                           `title` varchar(45) NOT NULL,\n" +
                "                           `author_ua_id` int NOT NULL,\n" +
                "                           `publisher_isbn` varchar(17) NOT NULL,\n" +
                "                           `date` date NOT NULL,\n" +
                "                           `description` varchar(500) NOT NULL,\n" +
                "                           `cover_id` int DEFAULT NULL,\n" +
                "                           PRIMARY KEY (`book_id`),\n" +
                "                           KEY `fk_book_ua_1_idx` (`author_ua_id`),\n" +
                "                           KEY `fk_book_ua_2_idx` (`publisher_isbn`),\n" +
                "                           KEY `fk_book_ua_1_idx1` (`cover_id`),\n" +
                "                           CONSTRAINT `book_ua_book_id_fk` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`),\n" +
                "                           CONSTRAINT `fk_book_ua_author_ua` FOREIGN KEY (`author_ua_id`) REFERENCES `author_ua` (`author_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,\n" +
                "                           CONSTRAINT `fk_book_ua_cover_id` FOREIGN KEY (`cover_id`) REFERENCES `cover` (`id`),\n" +
                "                           CONSTRAINT `fk_book_ua_publisher_isbn` FOREIGN KEY (`publisher_isbn`) REFERENCES `publisher` (`isbn`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci";

        String dropBooksGenresQuery = "DROP TABLE IF EXISTS `book_ua_has_genre_ua`";
        String createBooksGenresQuery = "CREATE TABLE `book_ua_has_genre_ua` (\n" +
                "                                        `book_ua_id` int NOT NULL,\n" +
                "                                        `genre_ua_id` int NOT NULL,\n" +
                "                                        PRIMARY KEY (`book_ua_id`,`genre_ua_id`),\n" +
                "                                        KEY `fk_genre_ua_id_idx` (`genre_ua_id`),\n" +
                "                                        CONSTRAINT `fk_book_ua_id` FOREIGN KEY (`book_ua_id`) REFERENCES `book_ua` (`book_id`),\n" +
                "                                        CONSTRAINT `fk_genre_ua_id` FOREIGN KEY (`genre_ua_id`) REFERENCES `genre_ua` (`genre_id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci";

        Connection connection = HikariCPDataSource.getInstance().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropBookQuery);
        statement.executeUpdate(createBookQuery);
        statement.executeUpdate(dropBooksGenresQuery);
        statement.executeUpdate(createBooksGenresQuery);
    }

    @SneakyThrows
    private static void setUpOriginalBookTables() {
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

    private static void setUpDao() {
        SqlDaoFactory sqlDaoFactory = new SqlDaoFactory();
        dao = sqlDaoFactory.createBookDao(new Locale("uk", "ua"));
        originalDao = sqlDaoFactory.createBookDao(Locale.ENGLISH);
        authorDao = sqlDaoFactory.createAuthorDao(new Locale("uk", "ua"));
        authorOriginalDao = sqlDaoFactory.createAuthorDao(Locale.ENGLISH);
        publisherDao = sqlDaoFactory.createPublisherDao();
        genreDao = sqlDaoFactory.createGenreDao(new Locale("uk", "ua"));
        genreOriginalDao = sqlDaoFactory.createGenreDao(Locale.ENGLISH);
    }

    private static void setUpOriginals() {
        originalBook = BookEnSqlDaoTest.createBook();
        authorOriginalDao.save(originalBook.getAuthor());
        publisherDao.save(originalBook.getPublisher());
        originalDao.save(originalBook);
    }

    @Order(1)
    @Test
    void save() {
        dao.save(book);
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
        book.setTitle("Не тест");
        book.setDate(LocalDate.now());
        dao.update(book);
        find();
    }

    @Disabled
    @Order(5)
    @Test
    void saveGenres() {
        setUpOriginalGenres();
        setUpGenres();
        dao.saveGenres(book);
    }

    private static void setUpOriginalGenres() {
        Genre originalGenre1 = new GenreImpl();
        originalGenre1.setName("Genre1");
        Genre originalGenre2 = new GenreImpl();
        originalGenre2.setName("Genre2");
        genreOriginalDao.save(originalGenre1);
        genreOriginalDao.save(originalGenre2);

        List<Genre> originalGenres = new ArrayList<>();
        originalGenres.add(originalGenre1);
        originalGenres.add(originalGenre2);

        originalBook.setGenres(originalGenres);
        originalDao.saveGenres(originalBook);
    }

    private static void setUpGenres() {
        Genre genre1 = new GenreImpl();
        genre1.setId(originalBook.getGenres().get(0).getId());
        genre1.setName("Жанр1");
        Genre genre2 = new GenreImpl();
        genre2.setId(originalBook.getGenres().get(1).getId());
        genre2.setName("Жанр2");
        genreDao.save(genre1);
        genreDao.save(genre2);

        List<Genre> genres = new ArrayList<>();
        genres.add(genre1);
        genres.add(genre2);

        for (Genre genre : genres) {
            genreDao.save(genre);
        }

        book.setGenres(genres);
    }
    @Disabled
    @Order(6)
    @Test
    void findGenres() {
        List<Genre> genres = dao.findGenres(book.getId());
        assertEquals(book.getGenres(), genres);
    }
    @Disabled
    @Order(7)
    @Test
    void updateGenres() {
        Genre originalGenre3 = new GenreImpl();
        originalGenre3.setName("Genre3");
        genreOriginalDao.save(originalGenre3);

        List<Genre> genres = book.getGenres();
        genres.add(originalGenre3);
        originalBook.setGenres(genres);

        Genre genre1 = new GenreImpl();
        genre1.setId(originalBook.getGenres().get(2).getId());
        genre1.setName("Жанр1");

        originalDao.updateGenres(originalBook);


        findGenres();
    }
    @Disabled
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
        dropOriginalTables();
        String dropBooksGenresQuery = "DROP TABLE book_ua_has_genre_ua";
        String dropBookQuery = "DROP TABLE `book_ua`";

        Connection connection = HikariCPDataSource.getInstance().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropBooksGenresQuery);
        statement.executeUpdate(dropBookQuery);

        GenreUaSqlDaoTest.dropTables();
        AuthorUaSqlDaoTest.dropTables();
        BookEnSqlDaoTest.dropTables();
    }

    @SneakyThrows
    static void dropOriginalTables() {
        String dropBooksGenresQuery = "DROP TABLE book_has_genre";
        String dropBookQuery = "DROP TABLE `book`";
        String dropCoverQuery = "DROP TABLE `cover`";

        Connection connection = HikariCPDataSource.getInstance().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropBooksGenresQuery);
        statement.executeUpdate(dropBookQuery);
        statement.executeUpdate(dropCoverQuery);
    }
}
