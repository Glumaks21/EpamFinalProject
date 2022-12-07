package ua.maksym.hlushchenko.db.dao.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.db.HikariCPDataSource;
import ua.maksym.hlushchenko.db.entity.Genre;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GenreSqlDaoTest {
    private static Connection connection;
    private static GenreSqlDao dao;
    private static Genre genre;

    static Genre createGenre() {
        Genre genre = new Genre();
        genre.setName("test");
        return genre;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        connection = HikariCPDataSource.getConnection();
        dao = new GenreSqlDao(connection);
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
        connection.close();
    }
}