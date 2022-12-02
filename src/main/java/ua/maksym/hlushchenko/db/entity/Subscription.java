package ua.maksym.hlushchenko.db.entity;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Subscription {
    private int id;
    private Reader reader;
    private Book book;
    private LocalDate takenDate;
    private LocalDate broughtDate;
    private double fine;
}
