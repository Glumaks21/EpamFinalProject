package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.role.User;

import java.util.Optional;

public interface UserDao extends Dao<Integer, User> {
    Optional<User> findByLogin(String login);
}
