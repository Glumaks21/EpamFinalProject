package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.role.Reader;

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
