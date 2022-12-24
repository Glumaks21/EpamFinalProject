package ua.maksym.hlushchenko.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import org.slf4j.*;

import java.io.IOException;

@WebFilter("/*")
public class LoggingFilter extends HttpFilter {
    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected void doFilter(HttpServletRequest req,
                            HttpServletResponse res,
                            FilterChain chain)
            throws IOException, ServletException {
        System.out.println("Received " + req.getRequestURL());
        chain.doFilter(req, res);
    }
}
