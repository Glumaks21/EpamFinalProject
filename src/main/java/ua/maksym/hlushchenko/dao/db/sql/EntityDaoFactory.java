package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.impl.Book;
import ua.maksym.hlushchenko.dao.entity.impl.Publisher;
import ua.maksym.hlushchenko.dao.entity.impl.Receipt;
import ua.maksym.hlushchenko.exception.ConnectionException;
import ua.maksym.hlushchenko.exception.EntityNotFoundException;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class EntityDaoFactory {
    private final EntityManager entityManager;

    public EntityDaoFactory(EntityManager entityManager) {
        Objects.requireNonNull(entityManager);
        this.entityManager = entityManager;
    }

    public <K, T> Dao<K, T> getDao(Class<T> entityClass) {
        if (!entityManager.isContainsEntity(entityClass)) {
            throw new EntityNotFoundException("Entity of class " + entityClass + " is not scanned");
        }

        try {
            return new GenericDao<>(entityClass, HikariCPDataSource.getInstance().getConnection());
        } catch (SQLException e) {
            throw new ConnectionException(e);
        }
    }
}

class Test {
    public static void main(String[] args) {
        EntityManager entityManager = new EntityManager();
        entityManager.scanPackage("ua.maksym.hlushchenko.dao.entity.impl");
        EntityDaoFactory entityDaoFactory = new EntityDaoFactory(entityManager);

        Dao<Integer, Book> dao = entityDaoFactory.getDao(Book.class);
        List<Book> books = dao.findAll();
        System.out.println(books);
        System.out.println(books.get(0).getGenres());
        System.out.println(books.get(0).getGenres());
        System.out.println(books);
    }
}
