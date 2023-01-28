package ua.maksym.hlushchenko.dao.db.sql;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import ua.maksym.hlushchenko.orm.dao.Dao;
import ua.maksym.hlushchenko.dao.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.Author;
import ua.maksym.hlushchenko.orm.dao.GenericDao;
import ua.maksym.hlushchenko.orm.dao.SessionImpl;
import ua.maksym.hlushchenko.orm.entity.EntityParser;

import java.sql.*;
import java.util.List;
import java.util.Optional;

import static ua.maksym.hlushchenko.dao.db.sql.TestEntityFactory.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthorGenericDaoTest {
    private static SessionImpl session;
    private static Dao<Integer, Author> dao;

    @BeforeAll
    static void init() throws SQLException {
        session = new SessionImpl(HikariCPDataSource.getInstance().getConnection());
        dao = new GenericDao<>(Author.class, session);
    }

    @BeforeEach
    @AfterEach
    void clearTable() {
        try (Connection connection = HikariCPDataSource.getInstance().getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            statement.executeUpdate("DELETE FROM " + EntityParser.getTableNameOf(Author.class));
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void find()  {
        Author author = createEntity(Author.class);

        dao.save(author);
        session.commit();

        assertTrue(author.getId() != 0);

        Optional<Author> fromDb = dao.find(author.getId());
        assertTrue(fromDb.isPresent());
        assertEquals(author, fromDb.get());
    }

    @Test
    void findAll() {
        Author author1 = createEntity(Author.class);
        Author author2 = createEntity(Author.class);

        dao.save(author1);
        dao.save(author2);
        session.commit();

        List<Author> authorsIbDb = dao.findAll();
        assertTrue(authorsIbDb.contains(author1));
        assertTrue(authorsIbDb.contains(author2));
    }

    @Test
    void save() {
        Author author = createEntity(Author.class);

        dao.save(author);
        session.commit();

        assertTrue(author.getId() != 0);
    }

    @Test
    void update() {
        Author author = createEntity(Author.class);

        dao.save(author);
        session.commit();

        author = dao.find(author.getId()).get();
        author.setSurname("TestSuccessfully");
        dao.update(author);
        session.commit();

        author = dao.find(author.getId()).get();
        assertEquals("TestSuccessfully", author.getSurname());
    }

    @Test
    void delete() {
        Author author = createEntity(Author.class);

        dao.save(author);
        dao.delete(author.getId());
        session.commit();

        Optional<Author> authorInDb = dao.find(author.getId());
        assertTrue(authorInDb.isEmpty());
    }
}
