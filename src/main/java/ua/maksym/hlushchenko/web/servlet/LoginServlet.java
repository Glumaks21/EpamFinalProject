package ua.maksym.hlushchenko.web.servlet;

import ua.maksym.hlushchenko.dao.DaoFactory;
import ua.maksym.hlushchenko.dao.UserDao;
import ua.maksym.hlushchenko.dao.entity.role.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import ua.maksym.hlushchenko.exception.ParamsValidationException;
import ua.maksym.hlushchenko.util.ParamsValidator;
import ua.maksym.hlushchenko.util.Sha256Encoder;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/profile/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/login.jsp").
                forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (req.getSession(false) != null) {
            req.getRequestDispatcher("/").forward(req, resp);
            return;
        }

        String login = ParamsValidator.getRequiredParam(req, "login");
        String password = ParamsValidator.getRequiredParam(req, "password");

        DaoFactory daoFactory = (DaoFactory) req.getAttribute("daoFactory");
        UserDao dao = daoFactory.createUserDao();
        Optional<User> optionalUser = dao.findByLogin(login);
        if (optionalUser.isPresent()) {
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", optionalUser.get().getId());
//            session.setAttribute("role", optionalUser.get().getRole().getName());
            resp.sendRedirect("/");
        } else {
            req.setAttribute("message", "Incorrect login or password");
            resp.sendRedirect("/profile/login");
        }
    }
}
