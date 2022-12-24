package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.Genre;
import ua.maksym.hlushchenko.dao.entity.impl.GenreImpl;

import java.sql.Connection;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GenreEnSqlDaoTest extends SqlDaoTestHelper {
    private static Connection connection;
    private static GenreEnSqlDao dao;
    private static Genre genre;

    static Genre createGenre() {
        Genre genre = new GenreImpl();
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

    @SneakyThrows
    @AfterAll
    static void destroy() {
        clearTables();
        connection.close();
    }
}