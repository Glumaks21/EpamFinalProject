package ua.maksym.hlushchenko.dao;

import ua.maksym.hlushchenko.dao.entity.role.AbstractUser;
import ua.maksym.hlushchenko.orm.dao.Dao;

import java.util.Optional;

public interface UserDao extends Dao<Integer, AbstractUser> {
    Optional<AbstractUser> findByLogin(String login);
}
