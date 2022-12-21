package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.entity.Genre;
import ua.maksym.hlushchenko.dao.entity.impl.GenreImpl;

import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GenreUaSqlDaoTest extends SqlDaoTestHelper {
    private static SqlDaoFactory sqlDaoFactory;
    private static GenreSqlDao dao;
    private static Genre genre;

    static Genre createGenre() {
        Genre genre = new GenreImpl();
        genre.setName("Тест");
        return genre;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        createGenre();
        sqlDaoFactory = new SqlDaoFactory();

        GenreSqlDao daoOriginal = sqlDaoFactory.createGenreDao(Locale.ENGLISH);
        Genre genreOriginal = GenreEnSqlDaoTest.createGenre();
        daoOriginal.save(genreOriginal);

        dao = sqlDaoFactory.createGenreDao(new Locale("uk", "UA"));
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
        clearTables();
        sqlDaoFactory.close();
    }
}
