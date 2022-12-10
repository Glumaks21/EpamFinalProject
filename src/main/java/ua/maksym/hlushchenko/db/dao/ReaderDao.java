package ua.maksym.hlushchenko.db.dao;

import ua.maksym.hlushchenko.db.entity.*;
import ua.maksym.hlushchenko.db.entity.role.Reader;

import java.util.List;

public interface ReaderDao extends Dao<String, Reader> {
    List<Receipt> findReceipts();
    List<Subscription> findSubscriptions();
}
