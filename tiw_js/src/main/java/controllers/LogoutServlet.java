package controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve the current session and do not create one if it doesn't exist
        HttpSession session = request.getSession(false);

        // Check if a session exists
        if (session != null) {
            // Invalidate the session to clear all session attributes and remove it
            session.invalidate();
            System.out.println("Session invalidated successfully.");
        } else {
            System.out.println("No session found to invalidate.");
        }

        // Redirect to the login page after logout
        // Ensure the path is correct based on your application's directory structure
        response.sendRedirect("login.html");
    }
}
