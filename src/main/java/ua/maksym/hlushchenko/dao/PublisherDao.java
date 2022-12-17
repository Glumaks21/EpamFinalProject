package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.Publisher;

import java.util.Optional;

public interface PublisherDao<K> extends Dao<K, Publisher> {
    Optional<Publisher> findByName(String name);
    void deleteByName(String name);
}
