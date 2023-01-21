package ua.maksym.hlushchenko.dao.entity.impl;

import lombok.*;
import ua.maksym.hlushchenko.dao.entity.impl.role.Reader;
import ua.maksym.hlushchenko.orm.annotations.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Getter
@Setter
@ToString(exclude = "books", doNotUseGetters = true)
@EqualsAndHashCode(exclude = {"reader", "dateTime", "books"})
@Table("receipt")
public class Receipt {
    @Id(value = "id", autoGenerated = true)
    private int id;

    @ManyToOne(lazyInit = true)
    @JoinColumn("reader_id")
    private Reader reader;

    @Column("time")
    private LocalDateTime dateTime;

    @ManyToMany(genericType = Book.class)
    @JoinTable(name = "receipt_has_book",
            joinColumn = "receipt_id",
            inverseColumn = "book_id")
    private List<Book> books = new ArrayList<>();


    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime.truncatedTo(ChronoUnit.SECONDS);
    }
}
