package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.Subscription;

import java.util.List;

public interface SubscriptionDao extends Dao<Integer, Subscription> {
    List<Subscription> findByReaderLogin(String login);
    void deleteByReaderLogin(String login);
}
