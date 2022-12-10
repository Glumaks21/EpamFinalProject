package ua.maksym.hlushchenko.db.entity.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ua.maksym.hlushchenko.db.dao.ReceiptDao;
import ua.maksym.hlushchenko.db.entity.Book;
import ua.maksym.hlushchenko.db.entity.Receipt;
import ua.maksym.hlushchenko.db.entity.model.role.ReaderModel;
import ua.maksym.hlushchenko.db.entity.role.Reader;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Data
public class ReceiptModel implements Receipt {
    private int id;
    private Reader reader;
    private LocalDateTime dateTime;

    private Set<Book> books;


    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime.truncatedTo(ChronoUnit.SECONDS);
    }
}
