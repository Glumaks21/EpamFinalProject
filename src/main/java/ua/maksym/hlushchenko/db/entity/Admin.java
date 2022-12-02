package ua.maksym.hlushchenko.db.entity;

import lombok.Data;

@Data
public class Admin extends User {
    private User user;
}
