package ua.maksym.hlushchenko.dao.entity.sql.role;

import ua.maksym.hlushchenko.dao.entity.role.Librarian;

public class LibrarianImpl extends UserImpl implements Librarian {
    @Override
    public Role getRole() {
        return Role.LIBRARIAN;
    }
}
