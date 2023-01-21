package ua.maksym.hlushchenko.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import ua.maksym.hlushchenko.dao.DaoFactory;
import ua.maksym.hlushchenko.dao.UserDao;
import ua.maksym.hlushchenko.dao.entity.impl.role.AbstractUser;
import ua.maksym.hlushchenko.util.ParamsValidator;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id = ParamsValidator.getRequiredParam(req, "id");
        DaoFactory daoFactory = (DaoFactory) req.getAttribute("daoFactory");
        UserDao dao = daoFactory.createUserDao();
        Optional<AbstractUser> user = dao.find(Integer.parseInt(id));
        if (user.isEmpty()) {
            resp.sendRedirect("/error.html");
            return;
        }

        req.setAttribute("user", user.get());
        req.getRequestDispatcher("/jsp/profile.jsp").forward(req, resp);
    }
}
