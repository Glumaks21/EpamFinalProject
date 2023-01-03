package ua.maksym.hlushchenko.dao.entity.sql.role;

import ua.maksym.hlushchenko.dao.entity.role.Admin;

public class AdminImpl extends UserImpl implements Admin {
    @Override
    public Role getRole() {
        return Role.ADMIN;
    }
}
