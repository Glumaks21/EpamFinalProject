package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.impl.Receipt;
import ua.maksym.hlushchenko.dao.entity.impl.Subscription;
import ua.maksym.hlushchenko.dao.entity.impl.role.Reader;

import java.util.List;

public interface ReaderDao extends Dao<Integer, Reader> {
    List<Receipt> findReceipts(Integer id);
    void saveReceipts(Reader reader);
    void updateReceipts(Reader reader);
    void deleteReceipts(Integer id);
    List<Subscription> findSubscriptions(Integer id);
    void saveSubscriptions(Reader reader);
    void updateSubscriptions(Reader reader);
    void deleteSubscriptions(Integer id);
}
