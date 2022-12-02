package ua.maksym.hlushchenko.db.entity;

import lombok.Data;

@Data
public class Reader {
    private User user;
    private boolean isBlocked;
}
