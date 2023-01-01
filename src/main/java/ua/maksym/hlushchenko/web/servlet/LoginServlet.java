package ua.maksym.hlushchenko.web.servlet;

import ua.maksym.hlushchenko.dao.*;
import ua.maksym.hlushchenko.dao.entity.role.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import ua.maksym.hlushchenko.exception.*;
import ua.maksym.hlushchenko.util.*;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/profile/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
            getServletContext().getRequestDispatcher("/jsp/login.jsp").
                    forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String login = ParamsValidator.getRequiredParam(req, "login");
            String password = ParamsValidator.getRequiredParam(req, "password");

            DaoFactory daoFactory = (DaoFactory) req.getAttribute("daoFactory");
            UserDao dao = daoFactory.createUserDao();
            Optional<User> optionalUser = dao.findByLogin(login);
            if (optionalUser.isEmpty() ||
                    !optionalUser.get().getPasswordHash().equals(Sha256Encoder.encode(password))) {
                throw new ParamsValidationException("Incorrect login or password");
            }

            HttpSession session = req.getSession(true);
            session.setAttribute("userId", optionalUser.get().getId());
            session.setAttribute("role", optionalUser.get().getRole());
            resp.sendRedirect("/");
        } catch (ParamsValidationException | DaoException e) {
            req.setAttribute("message", e.getMessage());
            doGet(req, resp);
        }
    }
}
