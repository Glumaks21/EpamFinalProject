package ua.maksym.hlushchenko.dao.entity.role;

public interface User {
    int getId();
    void setId(int id);
    String getLogin();
    void setLogin(String login);
    String getPasswordHash();
    void setPasswordHash(String password);

    Role getRole();
    enum Role {
        READER, LIBRARIAN, ADMIN
    }
}
