package ua.maksym.hlushchenko.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import ua.maksym.hlushchenko.dao.*;
import ua.maksym.hlushchenko.dao.entity.impl.Book;
import ua.maksym.hlushchenko.util.ParamsValidator;

import java.io.IOException;
import java.util.*;


@WebServlet("/book")
public class BookServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String bookId = ParamsValidator.getRequiredParam(req, "book_id");
        DaoFactory daoFactory = (DaoFactory) req.getAttribute("daoFactory");
        BookDao dao = daoFactory.createBookDao(Locale.ENGLISH);
        Optional<Book> optionalBook = dao.find(Integer.parseInt(bookId));
        if (optionalBook.isEmpty()) {
            resp.sendRedirect("/error.html");
            return;
        }
        req.setAttribute("book", optionalBook.get());
        req.getRequestDispatcher("/jsp/book.jsp").forward(req, resp);
    }
}
