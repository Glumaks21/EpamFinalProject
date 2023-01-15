package ua.maksym.hlushchenko.dao.entity.role;

public interface Librarian extends User {
    @Override
    default Role getRole() {
        return Role.LIBRARIAN;
    }
}
