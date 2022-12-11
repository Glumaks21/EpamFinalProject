package ua.maksym.hlushchenko.dao.entity.impl.role;

import lombok.Data;
import lombok.ToString;
import ua.maksym.hlushchenko.dao.entity.role.User;

@Data
@ToString(exclude = "password")
public class UserImpl implements User {
    private String login;
    private String password;
}
