package ua.maksym.hlushchenko.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import ua.maksym.hlushchenko.dao.*;
import ua.maksym.hlushchenko.dao.entity.Book;
import ua.maksym.hlushchenko.util.ParamsValidator;

import java.io.IOException;
import java.util.*;

@WebServlet("/")
public class HomeServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        DaoFactory daoFactory = (DaoFactory) req.getAttribute("daoFactory");
        BookDao dao = daoFactory.createBookDao(Locale.ENGLISH);
//        List<Book> books = dao.findAll();
//        sort(req, books);
//        req.setAttribute("books", books);
        getServletContext().getRequestDispatcher("/jsp/home.jsp").
                forward(req, resp);
    }

    private void sort(HttpServletRequest req, List<Book> books) {
        Optional<String> optionalSortParam = ParamsValidator.getOptionalParam(req, "sort");

        if (optionalSortParam.isPresent()) {
            String param = optionalSortParam.get();
//            Comparator<Book> cmp = null;
//
//            if (param.equals("title_asc")) {
//                cmp = Comparator.comparing(Book::getTitle);
//            } else if (param.equals("title_desc")) {
//                cmp = Comparator.comparing(Book::getTitle).reversed();
//            } else if (param.startsWith("author_")) {
//                Comparator<Book> cmpByName = Comparator.comparing(b -> b.getAuthor().getName());
//                Comparator<Book> cmpBySurname = Comparator.comparing(b -> b.getAuthor().getSurname());
//                Comparator<Book> cmpByAuthor = cmpByName.thenComparing(cmpBySurname);
//
//                if (param.endsWith("asc")) {
//                    cmp = cmpByAuthor;
//                } else if (param.endsWith("desc")) {
//                    cmp = cmpByAuthor.reversed();
//                }
//            } else if (param.startsWith("publisher_")) {
//                Comparator<Book> cmpByPublisher = Comparator.comparing(b -> b.getPublisher().getName());
//
//                if (param.endsWith("esc")) {
//                    cmp = cmpByPublisher;
//                } else if (param.startsWith("desc")) {
//                    cmp = cmpByPublisher.reversed();
//                }
//            } else if (param.equals("date_asc")) {
//                cmp = Comparator.comparing(Book::getDate);
//            } else if (param.equals("date_desc")) {
//                cmp = Comparator.comparing(Book::getDate).reversed();
//            }


            //books.sort(cmp);
        }
    }
}
