package ua.maksym.hlushchenko.web.servlet;

import jakarta.servlet.ServletConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import ua.maksym.hlushchenko.dao.BookDao;
import ua.maksym.hlushchenko.dao.DaoFactory;
import ua.maksym.hlushchenko.dao.db.sql.SqlDaoFactory;
import ua.maksym.hlushchenko.dao.entity.Book;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@WebServlet("/")
public class HomeServlet extends HttpServlet {
    private static DaoFactory daoFactory;

    @Override
    public void init(ServletConfig config) throws ServletException {
        daoFactory = new SqlDaoFactory();
        super.init(config);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        BookDao dao = daoFactory.createBookDao(Locale.ENGLISH);
        List<Book> books = dao.findAll();
        req.setAttribute("books", books);
        getServletContext().getRequestDispatcher("/home.jsp").
                forward(req, resp);
    }
}
