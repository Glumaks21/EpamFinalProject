package ua.maksym.hlushchenko.db.dao;

import ua.maksym.hlushchenko.db.entity.Publisher;

import java.util.Optional;

public interface PublisherDao extends Dao<String, Publisher> {
    Optional<Publisher> findByName(String name);
    void deleteByName(String name);
}
