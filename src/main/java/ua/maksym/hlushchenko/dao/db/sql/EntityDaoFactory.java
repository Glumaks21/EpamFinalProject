package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.entity.Book;
import ua.maksym.hlushchenko.dao.entity.Genre;
import ua.maksym.hlushchenko.dao.entity.Subscription;
import ua.maksym.hlushchenko.dao.entity.impl.BookImpl;
import ua.maksym.hlushchenko.dao.entity.impl.GenreImpl;
import ua.maksym.hlushchenko.dao.entity.impl.SubscriptionImpl;
import ua.maksym.hlushchenko.dao.entity.impl.role.ReaderImpl;
import ua.maksym.hlushchenko.dao.entity.role.Reader;
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

    public<K, T, C extends T> Dao<K, T> getDao(Class<C> entityClass) {
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
        entityManager.scanPackage("ua.maksym.hlushchenko.dao.entity.impl.role");
        EntityDaoFactory entityDaoFactory = new EntityDaoFactory(entityManager);

        Dao<Integer, Reader> dao = entityDaoFactory.getDao(ReaderImpl.class);
        List<Reader> readers = dao.findAll();

        System.out.println(readers);
        System.out.println(readers);
    }
}
