package controllers;

import beans.User;
import dao.UserDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/register")
public class UserRegisterServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get the parameters from the request
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        if (username == null || email == null || password == null ||
                username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            response.getWriter().write("Incomplete registration details");
            return;
        }

        UserDAO userDAO = new UserDAO();

        try {
            if (userDAO.doesUsernameExist(username)) {
                response.getWriter().write("Username already exists");
                return;
            }

            if (userDAO.doesEmailExist(email)) {
                response.getWriter().write("Email already exists");
                return;
            }

            User user = new User(username, email, password);
            userDAO.registerUser(user);

            response.getWriter().write("Registration successful");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Registration failed due to an internal error");
            e.printStackTrace();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Registration failed due to an internal error");
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirect to the registration page
        response.sendRedirect("register.html");
    }
}
