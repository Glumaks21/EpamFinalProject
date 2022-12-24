package ua.maksym.hlushchenko.web.servlet;

import ua.maksym.hlushchenko.dao.DaoFactory;
import ua.maksym.hlushchenko.dao.ReaderDao;
import ua.maksym.hlushchenko.dao.entity.impl.role.ReaderImpl;
import ua.maksym.hlushchenko.dao.entity.role.Reader;
import ua.maksym.hlushchenko.util.ParamsValidator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/profile/registration")
public class RegistrationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/registration.jsp").
                forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String login = ParamsValidator.getRequiredParam(req, "login");
        String passwordHash = ParamsValidator.getRequiredParam(req, "passwordHash");

        DaoFactory daoFactory = (DaoFactory) req.getAttribute("daoFactory");
        ReaderDao readerDao = daoFactory.createReaderDao();

        Reader reader = new ReaderImpl();
        reader.setLogin(login);
        reader.setPasswordHash(passwordHash);
        readerDao.save(reader);

        resp.sendRedirect("/profile/login");
    }
}
