package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.Genre;
import ua.maksym.hlushchenko.dao.entity.impl.GenreImpl;

import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GenreUaSqlDaoTest {
    private static GenreSqlDao dao;
    private static GenreSqlDao daoOriginal;

    private static Genre genre;
    private static Genre genreOriginal;

    static Genre createGenre() {
        Genre genre = new GenreImpl();
        genre.setName("Тест");
        return genre;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        setUpTables();
        SqlDaoFactory sqlDaoFactory = new SqlDaoFactory();

        daoOriginal = sqlDaoFactory.createGenreDao(Locale.ENGLISH);
        genreOriginal = GenreEnSqlDaoTest.createGenre();
        daoOriginal.save(genreOriginal);

        dao = sqlDaoFactory.createGenreDao(new Locale("uk", "UA"));
        genre = createGenre();
        genre.setId(genreOriginal.getId());
    }

    @SneakyThrows
    static void setUpTables() {
        GenreEnSqlDaoTest.setUpTables();
        String dropQuery = "DROP TABLE IF EXISTS `genre_ua`";
        String createQuery = "CREATE TABLE `genre_ua` (\n" +
                "                            `genre_id` int NOT NULL,\n" +
                "                            `name` varchar(45) NOT NULL,\n" +
                "                            PRIMARY KEY (`genre_id`),\n" +
                "                            CONSTRAINT `fk_genre_ua_genre_id` FOREIGN KEY (`genre_id`) REFERENCES `genre` (`id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci";

        Connection connection = HikariCPDataSource.getInstance().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropQuery);
        statement.executeUpdate(createQuery);
    }

    @Order(1)
    @Test
    void save() {
        dao.save(genre);
    }

    @Order(2)
    @Test
    void findAll() {
        List<Genre> genres = dao.findAll();
        Assertions.assertTrue(genres.contains(genre));
    }

    @Order(3)
    @Test
    void find() {
        Optional<Genre> optionalGenreInDb = dao.find(genre.getId());
        Assertions.assertTrue(optionalGenreInDb.isPresent());
        Genre genreInDb = optionalGenreInDb.get();
        Assertions.assertEquals(genre, genreInDb);
    }

    @Order(4)
    @Test
    void update() {
        genre.setName("Не тест");
        dao.update(genre);
        find();
    }

    @Order(5)
    @Test
    void delete() {
        dao.delete(genre.getId());
        Optional<Genre> optionalGenreInDb = dao.find(genre.getId());
        Assertions.assertTrue(optionalGenreInDb.isEmpty());
    }

    @AfterAll
    static void destroy() {
        dropTables();
    }

    @SneakyThrows
    static void dropTables() {
        String dropQuery = "DROP TABLE `genre_ua`";
        Connection connection = HikariCPDataSource.getInstance().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropQuery);

        GenreEnSqlDaoTest.dropTables();
    }
}
