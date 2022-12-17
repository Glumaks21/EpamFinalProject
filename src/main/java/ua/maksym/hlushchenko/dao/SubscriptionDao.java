package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.Subscription;

import java.util.List;

public interface SubscriptionDao<K> extends Dao<K, Subscription> {
    List<Subscription> findByReaderId(K id);
    void deleteByReaderId(K id);
}
