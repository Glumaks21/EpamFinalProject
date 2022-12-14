package ua.maksym.hlushchenko.dao.entity;

import ua.maksym.hlushchenko.dao.entity.role.Reader;

import java.time.LocalDateTime;
import java.util.List;

public interface Receipt {
    int getId();
    void setId(int id);
    Reader getReader();
    void setReader(Reader reader);
    LocalDateTime getDateTime();
    void setDateTime(LocalDateTime dateTime);
    List<Book> getBooks();
    void setBooks(List<Book> books);
}
