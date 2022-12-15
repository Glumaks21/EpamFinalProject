package ua.maksym.hlushchenko.servlets;

import org.slf4j.*;
import ua.maksym.hlushchenko.dao.Dao;
import ua.maksym.hlushchenko.dao.db.HikariCPDataSource;
import ua.maksym.hlushchenko.dao.db.sql.UserSqlDao;
import ua.maksym.hlushchenko.dao.entity.role.User;
import ua.maksym.hlushchenko.exception.ParamsValidationException;
import ua.maksym.hlushchenko.util.ParamsValidator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/profile/login")
public class LoginServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(LoginServlet.class);

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

            Dao<String, User> dao = new UserSqlDao(HikariCPDataSource.getInstance());
            Optional<User> optionalUser = dao.find(loginParam);
            if (optionalUser.isEmpty() || !optionalUser.get().getLogin().equals(loginParam)) {
                throw new ParamsValidationException();
            }


        } catch (ParamsValidationException e) {
            log.warn(e.getMessage());
            resp.sendRedirect("/static/html/error.html");
        }
    }
}
