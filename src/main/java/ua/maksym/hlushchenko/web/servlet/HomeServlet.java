package ua.maksym.hlushchenko.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import ua.maksym.hlushchenko.dao.*;
import ua.maksym.hlushchenko.dao.entity.Book;

import java.io.IOException;
import java.util.*;

@WebServlet("/")
public class HomeServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        DaoFactory daoFactory = (DaoFactory) req.getAttribute("daoFactory");
        BookDao dao = daoFactory.createBookDao(Locale.ENGLISH);
        List<Book> books = dao.findAll();
        req.setAttribute("languages", List.of("UA", "EN"));
        req.setAttribute("books", books);
        getServletContext().getRequestDispatcher("/reader_home.jsp").
                forward(req, resp);
    }
}
