package ua.maksym.hlushchenko.db.dao;

import ua.maksym.hlushchenko.db.entity.Book;
import ua.maksym.hlushchenko.db.entity.Receipt;

import java.util.List;

public interface ReceiptDao extends Dao<Integer, Receipt> {
    List<Book> findBooks(Integer id);
    void saveBooks(Receipt receipt);
    void updateBooks(Receipt receipt);
    void deleteBooks(Integer integer);
}
