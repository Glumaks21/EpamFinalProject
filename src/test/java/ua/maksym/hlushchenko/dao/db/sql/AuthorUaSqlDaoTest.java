package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.Author;
import ua.maksym.hlushchenko.dao.entity.impl.AuthorImpl;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthorUaSqlDaoTest {
    private static SqlDaoFactory sqlDaoFactory;
    private static AuthorSqlDao dao;
    private static AuthorSqlDao daoOriginal;

    private static Author author;
    private static Author authorOriginal;

    static Author createAuthor() {
        AuthorImpl author = new AuthorImpl();
        author.setName("Барак");
        author.setSurname("Обама");
        return author;
    }

    @SneakyThrows
    @BeforeAll
    static void init() {
        setUpTables();
        sqlDaoFactory = new SqlDaoFactory();

        daoOriginal = sqlDaoFactory.createAuthorDao(Locale.ENGLISH);
        dao = sqlDaoFactory.createAuthorDao(new Locale("uk", "UA"));

        authorOriginal = AuthorEnSqlDaoTest.createAuthor();
        daoOriginal.save(authorOriginal);

        author = createAuthor();
        author.setId(authorOriginal.getId());
    }

    @SneakyThrows
    static void setUpTables() {
        AuthorEnSqlDaoTest.setUpTables();
        String dropQuery = "DROP TABLE IF EXISTS `author_ua`";
        String createQuery = "CREATE TABLE `author_ua` (\n" +
                "                             `author_id` int NOT NULL AUTO_INCREMENT,\n" +
                "                             `name` varchar(45) NOT NULL,\n" +
                "                             `surname` varchar(45) NOT NULL,\n" +
                "                             PRIMARY KEY (`author_id`),\n" +
                "                             CONSTRAINT `author_ua_author_id_fk` FOREIGN KEY (`author_id`) REFERENCES `author` (`id`)\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=464 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;";

        Connection connection = HikariCPDataSource.getInstance().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropQuery);
        statement.executeUpdate(createQuery);
    }


    @Order(1)
    @Test
    void save() {
        dao.save(author);
    }

    @Order(2)
    @Test
    void findAll() {
        List<Author> authors = dao.findAll();
        Assertions.assertTrue(authors.contains(author));
    }

    @Order(3)
    @Test
    void find() {
        Optional<Author> optionalAuthorInDb = dao.find(author.getId());
        Assertions.assertTrue(optionalAuthorInDb.isPresent());
        Author authorInDb = optionalAuthorInDb.get();
        Assertions.assertEquals(author, authorInDb);
    }

    @Order(4)
    @Test
    void update() {
        author.setName("Джо");
        author.setSurname("Біден");
        dao.update(author);
        find();
    }

    @Order(5)
    @Test
    void delete() {
        dao.delete(author.getId());
        Optional<Author> optionalAuthorInDb = dao.find(author.getId());
        Assertions.assertTrue(optionalAuthorInDb.isEmpty());
    }

    @SneakyThrows
    @AfterAll
    static void destroy() {
        dropTables();
        sqlDaoFactory.close();
    }

    @SneakyThrows
    static void dropTables() {
        String dropQuery = "DROP TABLE `author_ua`";
        Connection connection = HikariCPDataSource.getInstance().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropQuery);

        AuthorEnSqlDaoTest.dropTables();
    }
}