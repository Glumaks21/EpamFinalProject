package ua.maksym.hlushchenko.web.servlet;

import jakarta.servlet.ServletConfig;
import org.slf4j.*;
import ua.maksym.hlushchenko.dao.DaoFactory;
import ua.maksym.hlushchenko.dao.UserDao;
import ua.maksym.hlushchenko.dao.db.sql.SqlDaoFactory;
import ua.maksym.hlushchenko.dao.entity.role.User;
import ua.maksym.hlushchenko.exception.ParamsValidationException;
import ua.maksym.hlushchenko.util.ParamsValidator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import ua.maksym.hlushchenko.util.Sha256Encoder;

import java.io.IOException;
import java.util.Optional;

//@WebServlet("/profile/login")
public class LoginServlet extends HttpServlet {
    private static DaoFactory daoFactory;

    @Override
    public void init(ServletConfig config) throws ServletException {
        daoFactory = new SqlDaoFactory();
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/static/html/login.html").
                forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String loginParam = ParamsValidator.getRequiredParam(req, "login");
            String passwordParam = ParamsValidator.getRequiredParam(req, "password");

            UserDao dao = daoFactory.createUserDao();
            Optional<User> optionalUser = dao.findByLoginAndPasswordHash(loginParam,
                    Sha256Encoder.encode(passwordParam));
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();

                HttpSession session = req.getSession(true);
                session.setAttribute("user_id", user.getId());
                session.setAttribute("role", user.getRole().getName());
            }

            resp.sendRedirect("/");
        } catch (ParamsValidationException e) {
            resp.sendRedirect("/static/html/error.html");
        }
    }

}
