package ua.maksym.hlushchenko.dao.entity;

import ua.maksym.hlushchenko.dao.entity.role.Reader;

import java.time.LocalDate;

public interface Subscription {
    int getId();
    void setId(int id);
    Reader getReader();
    void setReader(Reader reader);
    Book getBook();
    void setBook(Book book);
    LocalDate getTakenDate();
    void setTakenDate(LocalDate date);
    LocalDate getBroughtDate();
    void setBroughtDate(LocalDate date);
    double getFine();
    void setFine(double fine);
}
