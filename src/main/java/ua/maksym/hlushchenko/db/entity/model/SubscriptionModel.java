package ua.maksym.hlushchenko.db.entity.model;

import lombok.Data;
import ua.maksym.hlushchenko.db.entity.Book;
import ua.maksym.hlushchenko.db.entity.Subscription;
import ua.maksym.hlushchenko.db.entity.model.role.ReaderModel;
import ua.maksym.hlushchenko.db.entity.role.Reader;

import java.time.LocalDate;

@Data
public class SubscriptionModel implements Subscription {
    private int id;
    private Reader reader;
    private Book book;
    private LocalDate takenDate;
    private LocalDate broughtDate;
    private double fine;
}
