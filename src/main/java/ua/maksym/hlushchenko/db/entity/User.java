package ua.maksym.hlushchenko.db.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "password")
public class User {
    private String login;
    private String password;
}
