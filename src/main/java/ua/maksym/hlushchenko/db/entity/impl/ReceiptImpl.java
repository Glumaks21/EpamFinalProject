package ua.maksym.hlushchenko.db.entity.impl;

import lombok.Data;
import ua.maksym.hlushchenko.db.entity.Book;
import ua.maksym.hlushchenko.db.entity.Receipt;
import ua.maksym.hlushchenko.db.entity.role.Reader;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Data
public class ReceiptImpl implements Receipt {
    private int id;
    private Reader reader;
    private LocalDateTime dateTime;

    private List<Book> books = new ArrayList<>();

    @Override
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime.truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Receipt)) return false;
        Receipt receipt = (Receipt) o;
        return getId() == receipt.getId() &&
                Objects.equals(getReader(), receipt.getReader()) &&
                Objects.equals(getDateTime(), receipt.getDateTime()) &&
                Objects.equals(getBooks(), receipt.getBooks());
    }

}
