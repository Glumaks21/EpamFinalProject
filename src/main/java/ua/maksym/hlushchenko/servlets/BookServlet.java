package ua.maksym.hlushchenko.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.slf4j.*;

import ua.maksym.hlushchenko.dao.BookDao;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.db.sql.BookSqlDao;
import ua.maksym.hlushchenko.dao.entity.Book;
import ua.maksym.hlushchenko.exception.ParamsValidationException;
import ua.maksym.hlushchenko.util.ParamsValidator;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/book")
public class BookServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(BookServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String idParam = ParamsValidator.getRequiredParam(req, "id");

            BookDao bookDao = new BookSqlDao(HikariCPDataSource.getInstance());
            Optional<Book> optionalBook = bookDao.find(Integer.parseInt(idParam));
            Book book = optionalBook.get();
            req.setAttribute("book", book);

            getServletContext().getRequestDispatcher("/book.jsp").
                    forward(req, resp);
        } catch (ParamsValidationException e) {
            log.warn(e.getMessage());
            resp.sendRedirect("/static/html/error.html");
        }
    }
}
