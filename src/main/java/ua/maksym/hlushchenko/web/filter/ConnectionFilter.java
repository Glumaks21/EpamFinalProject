package ua.maksym.hlushchenko.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import ua.maksym.hlushchenko.dao.db.sql.SqlDaoFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

@WebFilter("/*")
public class ConnectionFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest req,
                            HttpServletResponse res,
                            FilterChain chain)
            throws IOException, ServletException {
        DataSource dataSource = (DataSource) getServletContext().getAttribute("dataSource");
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            SqlDaoFactory daoFactory = new SqlDaoFactory(connection);
            req.setAttribute("daoFactory", daoFactory);
            chain.doFilter(req, res);
            connection.commit();
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
