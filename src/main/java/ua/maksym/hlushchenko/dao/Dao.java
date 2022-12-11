package ua.maksym.hlushchenko.dao;

import java.util.List;
import java.util.Optional;

public interface Dao<K, T> {
    List<T> findAll();
    Optional<T> find(K id);
    void save(T entity);
    void update(T entity);
    void delete(K id);
}
