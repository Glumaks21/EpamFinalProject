package ua.maksym.hlushchenko.web.servlet;

import org.slf4j.*;

import ua.maksym.hlushchenko.exception.ParamsValidationException;
import ua.maksym.hlushchenko.util.ParamsValidator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

public class RegistrationServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(RegistrationServlet.class);

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.info("Received request: " + req);
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/static/html/registration.html").
                forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String loginParam = ParamsValidator.getRequiredParam(req, "login");
            String passwordParam = ParamsValidator.getRequiredParam(req, "password");
            String passwordConfirmationParam = ParamsValidator.getRequiredParam(req, "password_confirmation");

//            if (!passwordParam.equals(passwordConfirmationParam)) {
//                throw new ParamsValidationException("Password are not the same");
//            }
//
//            UserSqlDao userSqlDao = new UserSqlDao(HikariCPDataSource.getInstance());
//            if (userSqlDao.find(loginParam).isPresent()) {
//                throw new ParamsValidationException("Login " + loginParam + " is already registered");
//            }
//
//            ReaderDao readerDao = new ReaderSqlDao(HikariCPDataSource.getInstance());
//            Reader reader = new ReaderImpl();
//            reader.setLogin(loginParam);
////            reader.setPassword(passwordParam);
//            readerDao.save(reader);

            resp.sendRedirect("/");
        } catch (ParamsValidationException e) {
            log.info(e.getMessage());
            resp.sendRedirect("/static/html/error.html");
        }
    }
}
