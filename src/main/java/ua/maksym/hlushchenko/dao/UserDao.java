package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.impl.role.AbstractUser;

import java.util.Optional;

public interface UserDao extends Dao<Integer, AbstractUser> {
    Optional<AbstractUser> findByLogin(String login);
}
