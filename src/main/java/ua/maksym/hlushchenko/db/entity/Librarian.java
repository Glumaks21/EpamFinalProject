package ua.maksym.hlushchenko.db.entity;

import lombok.Data;

@Data
public class Librarian extends User{
    private User user;
}