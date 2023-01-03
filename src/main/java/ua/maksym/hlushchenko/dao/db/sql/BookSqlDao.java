package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.BookDao;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.sql.BookImpl;
import ua.maksym.hlushchenko.exception.*;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

abstract class BookSqlDao extends AbstractSqlDao<Integer, Book> implements BookDao {
    private final Locale locale;

    public BookSqlDao(Connection connection, Locale locale) {
        super(connection);
        this.locale = locale;
    }
}
