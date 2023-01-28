package ua.maksym.hlushchenko.dao.db.sql;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.orm.dao.Dao;
import ua.maksym.hlushchenko.dao.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.Genre;
import ua.maksym.hlushchenko.dao.entity.GenreUa;
import ua.maksym.hlushchenko.orm.dao.GenericDao;
import ua.maksym.hlushchenko.orm.dao.SessionImpl;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ua.maksym.hlushchenko.dao.db.sql.TestEntityFactory.createEntity;


public class GenreUaGenericDaoTest {
    private static SessionImpl session;
    private static Dao<Integer, GenreUa> dao;

    @BeforeAll
    static void init() throws SQLException {
        session = new SessionImpl(HikariCPDataSource.getInstance().getConnection());
        dao = new GenericDao<>(GenreUa.class, session);
    }

    @BeforeEach
    @AfterEach
    void clearTable() {
        try (Connection connection = HikariCPDataSource.getInstance().getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            statement.executeUpdate("DELETE FROM " + "genre");
            statement.executeUpdate("DELETE FROM " + "genre_ua");
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void find() {
        Genre original = createEntity(Genre.class);
        Dao<Object, Genre> originalDao = new GenericDao<>(Genre.class, session);
        originalDao.save(original);

        GenreUa genreUa = createEntity(GenreUa.class);
        genreUa.setId(original.getId());

        dao.save(genreUa);
        session.commit();

        Optional<GenreUa> fromDb = dao.find(genreUa.getId());
        assertTrue(fromDb.isPresent());
        assertEquals(genreUa, fromDb.get());
    }

    @Test
    void findAll() {
        Genre original1 = createEntity(Genre.class);
        Genre original2 = createEntity(Genre.class);
        Dao<Object, Genre> originalDao = new GenericDao<>(Genre.class, session);
        originalDao.save(original1);
        originalDao.save(original2);

        GenreUa author1 = createEntity(GenreUa.class);
        author1.setId(original1.getId());
        GenreUa author2 = createEntity(GenreUa.class);
        author2.setId(original2.getId());

        dao.save(author1);
        dao.save(author2);
        session.commit();

        List<GenreUa> fromDb = dao.findAll();
        assertTrue(fromDb.contains(author1));
        assertTrue(fromDb.contains(author2));
    }

    @Test
    void save() {
        Genre original = createEntity(Genre.class);
        Dao<Object, Genre> originalDao = new GenericDao<>(Genre.class, session);
        originalDao.save(original);

        GenreUa genreUa = createEntity(GenreUa.class);
        genreUa.setId(original.getId());

        dao.save(genreUa);
        session.commit();
    }

    @Test
    void update() {
        Genre original = createEntity(Genre.class);
        Dao<Object, Genre> originalDao = new GenericDao<>(Genre.class, session);
        originalDao.save(original);

        GenreUa genreUa = createEntity(GenreUa.class);
        genreUa.setId(original.getId());

        dao.save(genreUa);
        session.commit();

        genreUa = dao.find(genreUa.getId()).get();
        genreUa.setName("Змінено");
        dao.update(genreUa);
        session.commit();

        genreUa = dao.find(genreUa.getId()).get();
        assertEquals("Змінено", genreUa.getName());
    }

    @Test
    void delete() {
        Genre original = createEntity(Genre.class);
        Dao<Object, Genre> originalDao = new GenericDao<>(Genre.class, session);
        originalDao.save(original);

        GenreUa genreUa = createEntity(GenreUa.class);
        genreUa.setId(original.getId());

        dao.save(genreUa);
        dao.delete(genreUa.getId());
        session.commit();

        Optional<GenreUa> fromDb = dao.find(genreUa.getId());
        assertTrue(fromDb.isEmpty());
    }

    @AfterAll
    static void destroy() {
        session.closeSession();
    }
}
