package ua.maksym.hlushchenko.db.entity.model.role;

import lombok.Data;
import lombok.ToString;
import ua.maksym.hlushchenko.db.entity.role.User;

@Data
@ToString(exclude = "password")
public class UserModel implements User {
    private String login;
    private String password;
}
