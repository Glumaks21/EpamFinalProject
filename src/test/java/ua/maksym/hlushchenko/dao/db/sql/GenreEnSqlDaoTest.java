package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.Genre;
import ua.maksym.hlushchenko.dao.entity.impl.GenreImpl;

import java.sql.*;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GenreEnSqlDaoTest {
    private static GenreSqlDao dao;
    private static Genre genre;

    static Genre createGenre() {
        Genre genre = new GenreImpl();
        genre.setName("test");
        return genre;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        setUpTables();

        SqlDaoFactory sqlDaoFactory = new SqlDaoFactory();
        dao = sqlDaoFactory.createGenreDao(Locale.ENGLISH);
        genre = createGenre();
    }

    @SneakyThrows
    static void setUpTables() {
        String dropQuery = "DROP TABLE IF EXISTS `genre`";
        String createQuery = "CREATE TABLE `genre` (\n" +
                "                         `id` int NOT NULL AUTO_INCREMENT,\n" +
                "                         `name` varchar(45) NOT NULL,\n" +
                "                         PRIMARY KEY (`id`),\n" +
                "                         UNIQUE KEY `name_UNIQUE` (`id`,`name`)\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=1497 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci";

        Connection connection = HikariCPDataSource.getInstance().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropQuery);
        statement.executeUpdate(createQuery);
    }

    @Order(1)
    @Test
    void save() {
        dao.save(genre);
        Assertions.assertTrue(genre.getId() != 0);
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
        genre.setName("ne_test");
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
        String dropQuery = "DROP TABLE `genre`";
        Connection connection = HikariCPDataSource.getInstance().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropQuery);
    }
}