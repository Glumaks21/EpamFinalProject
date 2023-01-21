package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.impl.Publisher;

import java.util.Optional;

public interface PublisherDao extends Dao<Integer, Publisher> {
    Optional<Publisher> findByName(String name);
    void deleteByName(String name);
}
