package controllers;

import dao.UserDAO;
import utils.ThymeleafConfig;
import utils.SecurityUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        ThymeleafConfig config = new ThymeleafConfig(getServletContext());
        this.templateEngine = config.getTemplateEngine();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SecurityUtils.isCsrfTokenValid(request)) {
            request.setAttribute("error", "Invalid CSRF token.");
            forwardToLoginPage(request, response);
            return;
        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            Integer userId = UserDAO.authenticate(username, password);
            if (userId != null) {
                HttpSession session = request.getSession();
                session.setAttribute("userId", userId);
                session.setAttribute("username", username);
                session.setMaxInactiveInterval(30 * 60); // Session expires after 30 minutes of inactivity
                response.sendRedirect(request.getContextPath() + "/protected/home");
            } else {
                request.setAttribute("error", "Authentication failed.");
                forwardToLoginPage(request, response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "An error occurred during authentication.");
            forwardToLoginPage(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        forwardToLoginPage(request, response);
    }

    private void forwardToLoginPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebContext context = new WebContext(request, response, getServletContext());
        context.setVariable("error", request.getAttribute("error"));
        context.setVariable("csrfToken", SecurityUtils.generateCsrfToken(request)); // Generate and set CSRF token
        templateEngine.process("login", context, response.getWriter());
    }
}
