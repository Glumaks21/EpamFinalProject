package ua.maksym.hlushchenko.web.listener;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;
import ua.maksym.hlushchenko.dao.HikariCPDataSource;

@WebListener
public class InitContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        context.setAttribute("dataSource", HikariCPDataSource.getInstance());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        HikariCPDataSource.getInstance().close();
    }
}
