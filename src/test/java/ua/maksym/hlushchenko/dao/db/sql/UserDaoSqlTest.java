package ua.maksym.hlushchenko.dao.db.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.impl.role.UserImpl;
import ua.maksym.hlushchenko.dao.entity.role.*;
import ua.maksym.hlushchenko.util.Sha256Encoder;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDaoSqlTest {
    private static UserSqlDao dao;
    private static User user;

    private static RoleSqlDao roleSqlDao;

    static User createUser() {
        UserImpl user = new UserImpl();
        user.setLogin("test");
        user.setPasswordHash(Sha256Encoder.encode("test"));
        user.setRole(RoleSqlDaoTest.createRole());
        return user;
    }

    @BeforeAll
    static void init() {
        setUpTables();
        SqlDaoFactory sqlDaoFactory = new SqlDaoFactory();
        dao = sqlDaoFactory.createUserDao();
        user = createUser();

        roleSqlDao = sqlDaoFactory.createRoleDao();
        roleSqlDao.save(user.getRole());
    }

    @SneakyThrows
    static void setUpTables() {
        RoleSqlDaoTest.setUpTables();

        String dropQuery = "DROP TABLE IF EXISTS `user`";
        String createQuery = "CREATE TABLE `user` (\n" +
                "                        `id` int NOT NULL AUTO_INCREMENT,\n" +
                "                        `login` varchar(45) NOT NULL,\n" +
                "                        `password_hash` varchar(256) NOT NULL,\n" +
                "                        `role_id` int NOT NULL,\n" +
                "                        PRIMARY KEY (`id`),\n" +
                "                        UNIQUE KEY `login_UNIQUE` (`login`),\n" +
                "                        KEY `fk_user_1_idx` (`role_id`,`id`),\n" +
                "                        CONSTRAINT `fk_user_role_id` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci";

        Connection connection = HikariCPDataSource.getInstance().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropQuery);
        statement.executeUpdate(createQuery);
    }

    @Order(1)
    @Test
    void save() {
        dao.save(user);
        assertTrue(user.getId() != 0);
    }

    @SneakyThrows
    @Order(2)
    @Test
    void findAll() {
        List<User> users = dao.findAll();
        assertTrue(users.contains(user));
    }

    @Order(3)
    @Test
    void find() {
        Optional<User> optionalUserInDb = dao.find(user.getId());
        assertTrue(optionalUserInDb.isPresent());
        User userInDb = optionalUserInDb.get();
        assertEquals(userInDb, user);
    }

    @Order(4)
    @Test
    void update() {
        user.setPasswordHash(Sha256Encoder.encode("ne_test"));
        dao.update(user);
        find();
    }

    @Order(5)
    @Test
    void delete() {
        dao.delete(user.getId());
        Optional<User> userInDb = dao.find(user.getId());
        assertTrue(userInDb.isEmpty());
    }

    @AfterAll
    static void destroy() {
        dropTables();
    }

    @SneakyThrows
    static void dropTables() {
        String dropQuery = "DROP TABLE `user`";
        Connection connection = HikariCPDataSource.getInstance().getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropQuery);

        RoleSqlDaoTest.dropTables();
    }
}