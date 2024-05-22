//package controllers;
//
//import dao.UserDAO;
//import javax.servlet.*;
//import javax.servlet.http.*;
//import javax.servlet.annotation.*;
//import java.io.IOException;
//import java.sql.SQLException;
//
//@WebServlet("/login")
//public class LoginServlet extends HttpServlet {
//    private static final long serialVersionUID = 1L;
//
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        String username = request.getParameter("username");
//        String password = request.getParameter("password");
//
//        UserDAO userDAO = new UserDAO();
//        try {
//            boolean isAuthenticated = userDAO.authenticateUser(username, password);
//
//            if (isAuthenticated) {
//                Integer userId = userDAO.getUserIdByUsername(username);
//                HttpSession existingSession = request.getSession(false);
//                if (existingSession != null) {
//                    existingSession.invalidate();
//                }
//                HttpSession newSession = request.getSession(true);
//                newSession.setAttribute("userId", userId);
//                response.sendRedirect("home.html");
//            } else {
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Send 401 status instead of redirecting
//                response.getWriter().write("Invalid credentials");
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            response.getWriter().write("Login failed due to an internal error");
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            response.getWriter().write("Unexpected error");
//        }
//    }
//
//}
//
package controllers;

import dao.UserDAO;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;
import org.json.JSONObject; // Make sure to include the org.json library

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        UserDAO userDAO = new UserDAO();
        JSONObject jsonResponse = new JSONObject();

        try {
            boolean isAuthenticated = userDAO.authenticateUser(username, password);
            if (isAuthenticated) {
                int userId = userDAO.getUserIdByUsername(username);
                HttpSession session = request.getSession(true);
                session.setAttribute("userId", userId);

                jsonResponse.put("status", "success");
                jsonResponse.put("userId", userId);
            } else {
                jsonResponse.put("status", "fail");
                jsonResponse.put("message", "Invalid credentials");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (SQLException e) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Login failed due to an internal error");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Unexpected error");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
        System.out.println("Sending JSON response: " + jsonResponse.toString()); // Log the JSON response
    }

}
