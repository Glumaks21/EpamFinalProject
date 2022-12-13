package ua.maksym.hlushchenko.dao.entity.impl;

import lombok.Data;
import ua.maksym.hlushchenko.dao.entity.Book;
import ua.maksym.hlushchenko.dao.entity.Subscription;
import ua.maksym.hlushchenko.dao.entity.role.Reader;

import java.time.LocalDate;

@Data
public class SubscriptionImpl implements Subscription {
    private int id;
    private Reader reader;
    private Book book;
    private LocalDate takenDate;
    private LocalDate broughtDate;
    private double fine;
}
