package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.Book;
import ua.maksym.hlushchenko.dao.entity.Receipt;

import java.util.List;

public interface ReceiptDao extends Dao<Integer, Receipt> {
    List<Book> findBooks(Integer id);
    void saveBooks(Receipt receipt);
    void updateBooks(Receipt receipt);
    void deleteBooks(Integer integer);
}
