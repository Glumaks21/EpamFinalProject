package ua.maksym.hlushchenko.dao.entity.impl.role;

import lombok.*;
import ua.maksym.hlushchenko.dao.entity.role.*;
import java.util.Objects;

@Data
@ToString(exclude = "passwordHash")
public abstract class UserImpl implements User {
    private int id;
    private String login;
    private String passwordHash;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return getId() == user.getId() &&
                getLogin().equals(user.getLogin()) &&
                getPasswordHash().equals(user.getPasswordHash());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLogin(), getPasswordHash());
    }
}
