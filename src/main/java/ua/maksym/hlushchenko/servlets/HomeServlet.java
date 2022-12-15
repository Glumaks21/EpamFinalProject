package ua.maksym.hlushchenko.servlets;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.BookDao;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.db.sql.BookSqlDao;
import ua.maksym.hlushchenko.dao.entity.Book;
import ua.maksym.hlushchenko.util.ParamsValidator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(HomeServlet.class);

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        BookDao dao = new BookSqlDao(HikariCPDataSource.getInstance());
        List<Book> books = dao.findAll();

        String author_id = ParamsValidator.getOptionalParam(req, "author_id");
        if (author_id != null) {
            books.removeIf(book -> {
                String bookAuthorId = String.valueOf(book.getAuthor().getId());
                return !bookAuthorId.equals(author_id);
            });
        }

        req.setAttribute("books", books);

        getServletContext().getRequestDispatcher("/home.jsp").
                forward(req, resp);
    }
}
