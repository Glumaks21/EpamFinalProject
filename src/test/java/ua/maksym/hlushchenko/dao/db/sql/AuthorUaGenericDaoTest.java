package ua.maksym.hlushchenko.dao.db.sql;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.impl.Author;
import ua.maksym.hlushchenko.dao.entity.impl.AuthorUa;

import java.sql.*;
import java.util.*;

import static ua.maksym.hlushchenko.dao.db.sql.TestEntityFactory.*;
import static org.junit.jupiter.api.Assertions.*;


public class AuthorUaGenericDaoTest {
    private static Session session;
    private static Dao<Integer, AuthorUa> dao;

    @BeforeAll
    static void init() throws SQLException {
        session = new Session(HikariCPDataSource.getInstance().getConnection());
        dao = new GenericDao<>(AuthorUa.class, session);
    }

    @BeforeEach
    @AfterEach
    void clearTable() {
        try (Connection connection = HikariCPDataSource.getInstance().getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            statement.executeUpdate("DELETE FROM " + "author");
            statement.executeUpdate("DELETE FROM " + "author_ua");
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void find() {
        Author original = createEntity(Author.class);
        Dao<Object, Author> originalDao = new GenericDao<>(Author.class, session);
        originalDao.save(original);

        AuthorUa author = createEntity(AuthorUa.class);
        author.setId(original.getId());

        dao.save(author);
        session.commit();

        Optional<AuthorUa> fromDb = dao.find(author.getId());
        assertTrue(fromDb.isPresent());
        assertEquals(author, fromDb.get());
    }

    @Test
    void findAll() {
        Author original1 = createEntity(Author.class);
        Author original2 = createEntity(Author.class);
        Dao<Object, Author> originalDao = new GenericDao<>(Author.class, session);
        originalDao.save(original1);
        originalDao.save(original2);

        AuthorUa author1 = createEntity(AuthorUa.class);
        author1.setId(original1.getId());
        AuthorUa author2 = createEntity(AuthorUa.class);
        author2.setId(original2.getId());

        dao.save(author1);
        dao.save(author2);
        session.commit();

        List<AuthorUa> authorsIbDb = dao.findAll();
        assertTrue(authorsIbDb.contains(author1));
        assertTrue(authorsIbDb.contains(author2));
    }

    @Test
    void save() {
        Author original = createEntity(Author.class);
        Dao<Object, Author> originalDao = new GenericDao<>(Author.class, session);
        originalDao.save(original);

        AuthorUa author = createEntity(AuthorUa.class);
        author.setId(original.getId());

        dao.save(author);
        session.commit();
    }

    @Test
    void update() {
        Author original = createEntity(Author.class);
        Dao<Object, Author> originalDao = new GenericDao<>(Author.class, session);
        originalDao.save(original);

        AuthorUa author = createEntity(AuthorUa.class);
        author.setId(original.getId());

        dao.save(author);
        session.commit();

        author = dao.find(author.getId()).get();
        author.setSurname("Змінено");
        dao.update(author);
        session.commit();

        author = dao.find(author.getId()).get();
        assertEquals("Змінено", author.getSurname());
    }

    @Test
    void delete() {
        Author original = createEntity(Author.class);
        Dao<Object, Author> originalDao = new GenericDao<>(Author.class, session);
        originalDao.save(original);

        AuthorUa author = createEntity(AuthorUa.class);
        author.setId(original.getId());

        dao.save(author);
        dao.delete(author.getId());
        session.commit();

        Optional<AuthorUa> authorInDb = dao.find(author.getId());
        assertTrue(authorInDb.isEmpty());
    }

    @AfterAll
    static void destroy() {
        session.closeSession();
    }
}
