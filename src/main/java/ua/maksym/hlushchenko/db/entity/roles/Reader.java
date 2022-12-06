package ua.maksym.hlushchenko.db.entity.roles;

import lombok.Data;

@Data
public class Reader {
    private User user;
    private boolean isBlocked;
}
