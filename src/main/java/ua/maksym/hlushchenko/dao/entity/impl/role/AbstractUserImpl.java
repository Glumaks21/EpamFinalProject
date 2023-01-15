package ua.maksym.hlushchenko.dao.entity.impl.role;

import lombok.*;
import ua.maksym.hlushchenko.dao.db.sql.annotations.*;
import ua.maksym.hlushchenko.dao.entity.role.*;
import java.util.Objects;

@Data
@ToString() //exclude = "passwordHash"
@Table("user")
public abstract class AbstractUserImpl implements User {
    @Id(value = "id", autoGenerated = true)
    private int id;

    @Column("login")
    private String login;

    @Column("password_hash")
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