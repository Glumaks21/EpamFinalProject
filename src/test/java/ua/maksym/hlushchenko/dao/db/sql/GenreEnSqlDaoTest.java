package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.impl.Genre;

import java.sql.Connection;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GenreEnSqlDaoTest extends SqlDaoTestHelper {
    private static Connection connection;
    private static GenreEnSqlDao dao;
    private static ua.maksym.hlushchenko.dao.entity.Genre genre;

    static ua.maksym.hlushchenko.dao.entity.Genre createGenre() {
        ua.maksym.hlushchenko.dao.entity.Genre genre = new Genre();
        genre.setName("test");
        return genre;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        clearTables();
        connection = HikariCPDataSource.getInstance().getConnection();
        dao = new GenreEnSqlDao(connection);
        genre = createGenre();
    }

    @Order(1)
    @Test2
    void save() {
        dao.save(genre);
        Assertions.assertTrue(genre.getId() != 0);
    }

    @Order(2)
    @Test2
    void findAll() {
        List<ua.maksym.hlushchenko.dao.entity.Genre> genres = dao.findAll();
        Assertions.assertTrue(genres.contains(genre));
    }

    @Order(3)
    @Test2
    void find() {
        Optional<ua.maksym.hlushchenko.dao.entity.Genre> optionalGenreInDb = dao.find(genre.getId());
        Assertions.assertTrue(optionalGenreInDb.isPresent());
        ua.maksym.hlushchenko.dao.entity.Genre genreInDb = optionalGenreInDb.get();
        Assertions.assertEquals(genre, genreInDb);
    }

    @Order(4)
    @Test2
    void update() {
        genre.setName("ne_test");
        dao.update(genre);
        find();
    }

    @Order(5)
    @Test2
    void delete() {
        dao.delete(genre.getId());
        Optional<ua.maksym.hlushchenko.dao.entity.Genre> optionalGenreInDb = dao.find(genre.getId());
        Assertions.assertTrue(optionalGenreInDb.isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        clearTables();
        connection.close();
    }
}