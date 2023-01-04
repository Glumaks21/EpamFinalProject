package ua.maksym.hlushchenko.web.servlet;

import ua.maksym.hlushchenko.dao.DaoFactory;
import ua.maksym.hlushchenko.dao.ReaderDao;
import ua.maksym.hlushchenko.dao.entity.impl.role.ReaderImpl;
import ua.maksym.hlushchenko.dao.entity.role.Reader;
import ua.maksym.hlushchenko.exception.DaoException;
import ua.maksym.hlushchenko.exception.ParamsValidationException;
import ua.maksym.hlushchenko.util.ParamsValidator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import ua.maksym.hlushchenko.util.Sha256Encoder;

import java.io.IOException;

@WebServlet("/profile/registration")
public class RegistrationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/jsp/registration.jsp").
                forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String login = ParamsValidator.getRequiredParam(req, "login");
            String password = ParamsValidator.getRequiredParam(req, "password");
            String passwordConfirmation = ParamsValidator.getRequiredParam(req, "password_confirmation");

            if (!passwordConfirmation.equals(password)) {
                throw new ParamsValidationException("Passwords do not match");
            }

            DaoFactory daoFactory = (DaoFactory) req.getAttribute("daoFactory");
            ReaderDao readerDao = daoFactory.createReaderDao();

            Reader reader = new ReaderImpl();
            reader.setLogin(login);
            reader.setPasswordHash(Sha256Encoder.encode(password));
            readerDao.save(reader);

            resp.sendRedirect("/profile/login");
        } catch (ParamsValidationException e) {
            req.setAttribute("message", e.getMessage());
            doGet(req, resp);
        } catch (DaoException e) {
            if (e.getMessage().startsWith("Duplicate entry")) {
                req.setAttribute("message", "login is already registered");
            } else {
                req.setAttribute("message", "try again a little later");
            }
            doGet(req, resp);
        }
    }
}
