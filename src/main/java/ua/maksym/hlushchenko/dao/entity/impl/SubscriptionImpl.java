package ua.maksym.hlushchenko.dao.entity.impl;

import lombok.Data;
import ua.maksym.hlushchenko.dao.entity.Book;
import ua.maksym.hlushchenko.dao.entity.Subscription;
import ua.maksym.hlushchenko.dao.entity.role.Reader;

import java.time.LocalDate;
import java.util.Objects;

@Data
public class SubscriptionImpl implements Subscription {
    private int id;
    private Reader reader;
    private Book book;
    private LocalDate takenDate;
    private LocalDate broughtDate;
    private double fine;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subscription)) return false;
        Subscription subscription = (Subscription) o;
        return getId() == subscription.getId() &&
                Double.compare(subscription.getFine(), getFine()) == 0 &&
                Objects.equals(getReader(), subscription.getReader()) &&
                Objects.equals(getBook(), subscription.getBook()) &&
                Objects.equals(getTakenDate(), subscription.getTakenDate()) &&
                Objects.equals(getBroughtDate(), subscription.getBroughtDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getReader(), getBook(), getTakenDate(), getBroughtDate(), getFine());
    }
}
