package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.Book;
import ua.maksym.hlushchenko.dao.entity.Receipt;

import java.util.List;

public interface ReceiptDao<K> extends Dao<K, Receipt> {
    List<Book> findBooks(K id);
    void saveBooks(Receipt receipt);
    void updateBooks(Receipt receipt);
    void deleteBooks(K id);
}
