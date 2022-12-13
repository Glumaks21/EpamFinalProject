package ua.maksym.hlushchenko.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.maksym.hlushchenko.dao.BookDao;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.db.sql.BookSqlDao;
import ua.maksym.hlushchenko.dao.entity.Book;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/")
public class HomeServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(HomeServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            BookDao dao = new BookSqlDao(HikariCPDataSource.getConnection());
            List<Book> books = dao.findAll();
            request.setAttribute("books", books);

            getServletContext().getRequestDispatcher("/library_page.jsp").
                    forward(request, response);
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }
    }
}
