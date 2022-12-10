package ua.maksym.hlushchenko.db.dao;

import ua.maksym.hlushchenko.db.entity.Subscription;
import ua.maksym.hlushchenko.db.entity.roles.Reader;

import java.util.Set;

public interface ReaderDao extends Dao<String, Reader> {
    Set<Subscription> getSubscriptions();
}
