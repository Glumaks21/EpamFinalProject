package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.role.Reader;

import java.util.List;

public interface ReaderDao<K> extends Dao<K, Reader> {
    List<Receipt> findReceipts(K id);
    void saveReceipts(Reader reader);
    void updateReceipts(Reader reader);
    void deleteReceipts(K id);

    List<Subscription> findSubscriptions(K id);
    void saveSubscriptions(Reader reader);
    void updateSubscriptions(Reader reader);
    void deleteSubscriptions(K id);
}
