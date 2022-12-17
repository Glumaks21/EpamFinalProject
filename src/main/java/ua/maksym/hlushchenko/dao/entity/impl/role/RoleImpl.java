package ua.maksym.hlushchenko.dao.entity.impl.role;

import lombok.Data;
import ua.maksym.hlushchenko.dao.entity.role.Role;

@Data
public class RoleImpl implements Role {
    private int id;
    private String name;
}
