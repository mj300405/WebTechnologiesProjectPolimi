package controllers;

import beans.Document;
import beans.Folder;
import dao.FolderDAO;
import dao.DocumentDAO;
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
import java.sql.Timestamp;
import java.util.List;

@WebServlet("/protected/contentManagement")
public class ContentManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private FolderDAO folderDAO;
    private DocumentDAO documentDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        ThymeleafConfig config = new ThymeleafConfig(getServletContext());
        this.templateEngine = config.getTemplateEngine();
        this.folderDAO = new FolderDAO();
        this.documentDAO = new DocumentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            HttpSession session = request.getSession(false);

            if (session == null || session.getAttribute("userId") == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            int userId = (Integer) session.getAttribute("userId");
            List<Folder> folders = folderDAO.getAllFoldersByUser(userId);

            WebContext context = new WebContext(request, response, getServletContext());
            context.setVariable("folders", folders);
            context.setVariable("folderOptionsHtml", buildFolderOptionsHtml(folders, 0));
            context.setVariable("message", request.getAttribute("message"));
            context.setVariable("error", request.getAttribute("error"));
            context.setVariable("csrfToken", session.getAttribute("csrfToken"));  // Pass CSRF token to the template
            templateEngine.process("contentManagement", context, response.getWriter());
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/protected/error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SecurityUtils.isCsrfTokenValid(request)) {
            request.setAttribute("error", "Invalid CSRF token.");
            doGet(request, response);
            return;
        }

        String action = request.getParameter("action");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        try {
            if ("createFolder".equals(action)) {
                createFolder(request, userId);
                request.setAttribute("message", "Folder created successfully.");
            } else if ("createDocument".equals(action)) {
                createDocument(request, userId);
                request.setAttribute("message", "Document created successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "An error occurred: " + e.getMessage());
        }
        doGet(request, response);  // Redirect back to the content management page to display the message
    }

    private void createFolder(HttpServletRequest request, int userId) throws SQLException {
        String folderName = request.getParameter("folderName");
        Integer parentId = null;
        if (request.getParameter("parentId") != null && !request.getParameter("parentId").isEmpty()) {
            parentId = Integer.parseInt(request.getParameter("parentId"));
        }
        Folder folder = new Folder(folderName, userId, parentId);
        folder.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        folderDAO.addFolder(folder);
    }

    private void createDocument(HttpServletRequest request, int userId) throws SQLException {
        String documentName = request.getParameter("documentName");
        String summary = request.getParameter("summary");
        String type = request.getParameter("type");
        int folderId = Integer.parseInt(request.getParameter("folderId"));
        
        // Ensure the user owns the folder they are adding the document to
        if (!folderDAO.isFolderOwnedByUser(folderId, userId)) {
            throw new SQLException("You do not have permission to add a document to this folder.");
        }

        Document document = new Document(documentName, userId, folderId, summary, type);
        document.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        documentDAO.addDocument(document);
    }

    private String buildFolderOptionsHtml(List<Folder> folders, int level) {
        StringBuilder sb = new StringBuilder();
        String prefix = "-- ".repeat(level);
        for (Folder folder : folders) {
            sb.append("<option value='").append(folder.getId()).append("'>")
              .append(prefix).append(folder.getName())
              .append("</option>");
            if (folder.getSubfolders() != null && !folder.getSubfolders().isEmpty()) {
                sb.append(buildFolderOptionsHtml(folder.getSubfolders(), level + 1));
            }
        }
        return sb.toString();
    }
}
