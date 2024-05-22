package controllers;

import dao.FolderDAO;
import beans.Folder;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/folders/*")
public class FolderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private FolderDAO folderDAO = new FolderDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in.");
            return;
        }

        Integer userId = (Integer) session.getAttribute("userId");
        try {
            List<Folder> folders = folderDAO.getAllFolders(userId);
            String json = Folder.toJson(folders);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving folders.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String folderName = request.getParameter("name");
        String parentIdParam = request.getParameter("parentId");
        Integer parentId = null;

        System.out.println("Received folderName: " + folderName);
        System.out.println("Received parentIdParam: " + parentIdParam);

        if (parentIdParam != null && !parentIdParam.trim().isEmpty() && !parentIdParam.equals("null")) {
            try {
                parentId = Integer.parseInt(parentIdParam);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid parent ID format");
                return;
            }
        }

        if (folderName == null || folderName.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Folder name cannot be empty");
            return;
        }

        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("User not authenticated");
                return;
            }

            Integer userId = (Integer) session.getAttribute("userId");
            Folder folder = new Folder(0, folderName, userId, parentId, null);
            folderDAO.addFolder(folder);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("Folder created successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error creating folder");
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Folder ID is required\"}");
            return;
        }

        try {
            int folderId = Integer.parseInt(idParam);
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"User not authenticated\"}");
                return;
            }

            boolean isDeleted = folderDAO.deleteFolderRecursively(folderId);
            if (isDeleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\":\"Folder deleted successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Folder not found\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid folder ID format\"}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error deleting folder\"}");
            e.printStackTrace();
        }
    }
}
