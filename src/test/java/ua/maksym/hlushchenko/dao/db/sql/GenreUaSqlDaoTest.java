package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.sql.GenreImpl;

import java.sql.Connection;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GenreUaSqlDaoTest extends SqlDaoTestHelper {
    private static Connection connection;
    private static GenreUaSqlDao dao;
    private static ua.maksym.hlushchenko.dao.entity.Genre genre;

    static ua.maksym.hlushchenko.dao.entity.Genre createGenre() {
        ua.maksym.hlushchenko.dao.entity.Genre genre = new GenreImpl();
        genre.setName("Тест");
        return genre;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        createGenre();
        connection = HikariCPDataSource.getInstance().getConnection();

        GenreEnSqlDao daoOriginal = new GenreEnSqlDao(connection);
        ua.maksym.hlushchenko.dao.entity.Genre genreOriginal = GenreEnSqlDaoTest.createGenre();
        daoOriginal.save(genreOriginal);

        dao = new GenreUaSqlDao(connection);
        genre = createGenre();
        genre.setId(genreOriginal.getId());
    }

    @Order(1)
    @Test
    void save() {
        dao.save(genre);
    }

    @Order(2)
    @Test
    void findAll() {
        List<ua.maksym.hlushchenko.dao.entity.Genre> genres = dao.findAll();
        Assertions.assertTrue(genres.contains(genre));
    }

    @Order(3)
    @Test
    void find() {
        Optional<ua.maksym.hlushchenko.dao.entity.Genre> optionalGenreInDb = dao.find(genre.getId());
        Assertions.assertTrue(optionalGenreInDb.isPresent());
        ua.maksym.hlushchenko.dao.entity.Genre genreInDb = optionalGenreInDb.get();
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
