package ua.maksym.hlushchenko.dao.db.sql;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.orm.dao.Dao;
import ua.maksym.hlushchenko.dao.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.Genre;
import ua.maksym.hlushchenko.orm.dao.GenericDao;
import ua.maksym.hlushchenko.orm.dao.SessionImpl;
import ua.maksym.hlushchenko.orm.entity.EntityParser;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GenreGenericDaoTest {
    private static SessionImpl session;
    private static Dao<Integer, Genre> dao;

    @BeforeAll
    static void init() throws SQLException {
        session = new SessionImpl(HikariCPDataSource.getInstance().getConnection());
        dao = new GenericDao<>(Genre.class, session);
    }

    @BeforeEach
    @AfterEach
    void clearTable() {
        try (Connection connection = HikariCPDataSource.getInstance().getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            statement.executeUpdate("DELETE FROM " + EntityParser.getTableNameOf(Genre.class));
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void find()  {
        Genre genre = TestEntityFactory.createEntity(Genre.class);

        dao.save(genre);
        session.commit();

        assertTrue(genre.getId() != 0);

        Optional<Genre> fromDb = dao.find(genre.getId());
        assertTrue(fromDb.isPresent());
        assertEquals(genre, fromDb.get());
    }

    @Test
    void findAll() {
        Genre genre1 = TestEntityFactory.createEntity(Genre.class);
        Genre genre2 = TestEntityFactory.createEntity(Genre.class);

        dao.save(genre1);
        dao.save(genre2);
        session.commit();

        List<Genre> authorsIbDb = dao.findAll();
        assertTrue(authorsIbDb.contains(genre1));
        assertTrue(authorsIbDb.contains(genre2));
    }

    @Test
    void save() {
        Genre genre = TestEntityFactory.createEntity(Genre.class);

        dao.save(genre);
        session.commit();

        assertTrue(genre.getId() != 0);
    }

    @Test
    void update() {
        Genre genre = TestEntityFactory.createEntity(Genre.class);

        dao.save(genre);
        session.commit();

        genre = dao.find(genre.getId()).get();
        genre.setName("TestSuccessfully");
        dao.update(genre);
        session.commit();

        genre = dao.find(genre.getId()).get();
        assertEquals("TestSuccessfully", genre.getName());
    }

    @Test
    void delete() {
        Genre genre = TestEntityFactory.createEntity(Genre.class);

        dao.save(genre);
        dao.delete(genre.getId());
        session.commit();

        Optional<Genre> authorInDb = dao.find(genre.getId());
        assertTrue(authorInDb.isEmpty());
    }

    @AfterAll
    static void destroy() {
        session.closeSession();
    }
}
