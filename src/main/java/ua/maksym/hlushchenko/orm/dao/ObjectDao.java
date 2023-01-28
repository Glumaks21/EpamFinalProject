package ua.maksym.hlushchenko.orm.dao;

public interface ObjectDao<K, T> extends Dao<K, T> {
    void remove(T entity);
}
