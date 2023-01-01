package ua.maksym.hlushchenko.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebFilter(urlPatterns = {"/profile/login", "/profile/registration", "/profile/logout"})
public class AuthorisationFilter extends HttpFilter {
    private final List<String> needLogin = List.of("/profile/logout");

    @Override
    protected void doFilter(HttpServletRequest req,
                            HttpServletResponse res,
                            FilterChain chain)
            throws IOException, ServletException {
        if ((checkLogin(req) && !needLogin.contains(req.getServletPath()))) {
            res.sendRedirect("/");
        } else {
            chain.doFilter(req, res);
        }
    }

    private boolean checkLogin(HttpServletRequest req) {
        return req.getSession() != null &&
                req.getSession().getAttribute("userId") != null;
    }
}
