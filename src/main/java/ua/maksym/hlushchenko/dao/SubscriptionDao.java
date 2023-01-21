package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.impl.Subscription;

import java.util.List;

public interface SubscriptionDao extends Dao<Integer, Subscription> {
    List<Subscription> findByReaderId(Integer id);
    void deleteByReaderId(Integer id);
}
