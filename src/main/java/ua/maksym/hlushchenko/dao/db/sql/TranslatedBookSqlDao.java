package ua.maksym.hlushchenko.dao.db.sql;

import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.exception.DaoException;

import java.sql.Connection;
import java.sql.Date;
import java.util.*;

abstract class TranslatedBookSqlDao extends BookSqlDao {
    private static final String SQL_UPDATE_IN_ORIGINAL = QueryUtil.createUpdate(
            BookEnSqlDao.SQL_TABLE_NAME,
            List.of(BookEnSqlDao.SQL_COLUMN_NAME_AUTHOR, BookEnSqlDao.SQL_COLUMN_NAME_PUBLISHER,
                    BookEnSqlDao.SQL_COLUMN_NAME_DATE),
            List.of(BookEnSqlDao.SQL_COLUMN_NAME_ID)
    );

    public TranslatedBookSqlDao(Connection connection, Locale locale) {
        super(connection, locale);
    }

    @Override
    public void update(Book book) {
        updateQuery(SQL_UPDATE_IN_ORIGINAL,
                book.getAuthor().getId(),
                book.getPublisher().getId(),
                Date.valueOf(book.getDate()),
                book.getId());
    }

    @Override
    public void saveGenres(Book book) {
        GenreEnSqlDao originalGenreDao = new GenreEnSqlDao(connection);

        List<Genre> originalGenres = new ArrayList<>();
        for (Genre translatedGenre : book.getGenres()) {
            Optional<Genre> optionalGenre = originalGenreDao.find(translatedGenre.getId());
            if (optionalGenre.isPresent()) {
                originalGenres.add(optionalGenre.get());
            } else {
                throw new DaoException("Genre not found");
            }
        }

        BookSqlDao bookSqlDao = new BookEnSqlDao(connection);
        List<Genre> uaGenres = book.getGenres();
        book.setGenres(originalGenres);
        bookSqlDao.saveGenres(book);
        book.setGenres(uaGenres);
    }

    @Override
    public void updateGenres(Book book) {
        deleteGenres(book.getId());
        saveGenres(book);
    }

    @Override
    public void deleteGenres(Integer id) {
        BookEnSqlDao bookOriginalSqlDao = new BookEnSqlDao(connection);
        bookOriginalSqlDao.deleteGenres(id);
    }
}
