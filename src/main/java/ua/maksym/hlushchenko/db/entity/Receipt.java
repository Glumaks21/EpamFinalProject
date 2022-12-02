package ua.maksym.hlushchenko.db.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Receipt {
    private int id;
    private LocalDateTime dateTime;
}
