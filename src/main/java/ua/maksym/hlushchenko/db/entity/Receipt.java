package ua.maksym.hlushchenko.db.entity;

import ua.maksym.hlushchenko.db.entity.role.Reader;

import java.time.LocalDateTime;
import java.util.Set;

public interface Receipt {
    int getId();
    void setId(int id);
    Reader getReader();
    void setReader(Reader reader);
    LocalDateTime getDateTime();
    void setDateTime(LocalDateTime dateTime);
    Set<Book> getBooks();
    void setBooks(Set<Book> books);
}
