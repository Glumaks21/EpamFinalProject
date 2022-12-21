package ua.maksym.hlushchenko.dao.entity.impl.role;

import lombok.Data;
import ua.maksym.hlushchenko.dao.entity.role.Role;

import java.util.Objects;

@Data
public class RoleImpl implements Role {
    private int id;
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return getId() == role.getId() &&
                Objects.equals(getName(), role.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }
}
