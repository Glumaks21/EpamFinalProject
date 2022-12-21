package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.Book;
import ua.maksym.hlushchenko.dao.entity.Receipt;

import java.util.List;
import java.util.Optional;

public interface ReceiptDao<K> extends Dao<K, Receipt> {
    Optional<Receipt> findByReaderId(int id);
    void deleteByReaderId(int id);
    List<Book> findBooks(K id);
    void saveBooks(Receipt receipt);
    void updateBooks(Receipt receipt);
    void deleteBooks(K id);
}
