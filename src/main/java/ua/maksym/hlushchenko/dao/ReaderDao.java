package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.role.Reader;

import java.util.List;

public interface ReaderDao extends Dao<String, Reader> {
    List<Receipt> findReceipts(String login);
    void saveReceipts(Reader reader);
    void updateReceipts(Reader reader);
    void deleteReceipts(String login);

    List<Subscription> findSubscriptions(String login);
    void saveSubscriptions(Reader reader);
    void updateSubscriptions(Reader reader);
    void deleteSubscriptions(String login);
}
