package controllers;

import beans.User;
import dao.UserDAO;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import utils.ThymeleafConfig;
import utils.SecurityUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        ThymeleafConfig config = new ThymeleafConfig(getServletContext());
        this.templateEngine = config.getTemplateEngine();
        this.userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebContext context = new WebContext(request, response, getServletContext());
        if (request.getParameter("success") != null) {
            context.setVariable("success", "Registration successful! Please log in.");
        }
        context.setVariable("csrfToken", request.getSession().getAttribute("csrfToken")); // Pass CSRF token to the template
        templateEngine.process("register", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SecurityUtils.isCsrfTokenValid(request)) {
            request.setAttribute("error", "Invalid CSRF token.");
            doGet(request, response);
            return;
        }

        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match");
            doGet(request, response);
            return;
        }

        try {
            if (userDAO.doesUsernameExist(username)) {
                request.setAttribute("error", "Username already exists");
                doGet(request, response);
                return;
            }

            User user = new User(username, email, password);
            userDAO.registerUser(user);
            response.sendRedirect("register?success=true");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("error");
        }
    }
}
