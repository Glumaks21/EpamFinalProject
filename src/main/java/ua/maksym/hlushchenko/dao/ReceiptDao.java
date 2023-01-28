package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.Book;
import ua.maksym.hlushchenko.dao.entity.Receipt;
import ua.maksym.hlushchenko.orm.dao.Dao;

import java.util.List;
import java.util.Optional;

public interface ReceiptDao extends Dao<Integer, Receipt> {
    Optional<Receipt> findByReaderId(int id);
    void deleteByReaderId(int id);
    List<Book> findBooks(Integer id);
    void saveBooks(Receipt receipt);
    void updateBooks(Receipt receipt);
    void deleteBooks(Integer id);
}
