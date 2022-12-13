package ua.maksym.hlushchenko.dao.entity.impl.role;

import lombok.Data;
import lombok.ToString;
import ua.maksym.hlushchenko.dao.entity.role.User;

import java.util.Objects;

@Data
@ToString(exclude = "password")
public class UserImpl implements User {
    private String login;
    private String password;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return getLogin().equals(user.getLogin()) &&
                getPassword().equals(user.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLogin(), getPassword());
    }
}
