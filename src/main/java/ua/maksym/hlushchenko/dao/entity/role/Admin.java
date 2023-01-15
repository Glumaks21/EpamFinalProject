package ua.maksym.hlushchenko.dao.entity.role;

public interface Admin extends User {
    @Override
    default Role getRole() {
        return Role.ADMIN;
    }
}
