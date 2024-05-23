package controllers;

import beans.Document;
import beans.Folder;
import dao.DocumentDAO;
import dao.FolderDAO;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import utils.ThymeleafConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/protected/content")
public class ContentServlet extends HttpServlet {
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
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int folderId;
        try {
            folderId = Integer.parseInt(request.getParameter("folderId"));
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid folder ID.");
            forwardToContentPage(request, response);
            return;
        }

        try {
            int userId = (Integer) session.getAttribute("userId");
            
            // Ensure the user owns the folder they are trying to access
            if (!folderDAO.isFolderOwnedByUser(folderId, userId)) {
                request.setAttribute("error", "You do not have permission to access this folder.");
                forwardToContentPage(request, response);
                return;
            }

            Folder folder = folderDAO.getFolderById(folderId);
            List<Folder> subfolders = folderDAO.getSubfoldersByFolderId(folderId);
            List<Document> documents = documentDAO.getDocumentsByFolder(folderId);

            request.setAttribute("folder", folder);
            request.setAttribute("subfolders", subfolders);
            request.setAttribute("documents", documents);

            // Example success message
            String successMessage = request.getParameter("success");
            if (successMessage != null) {
                request.setAttribute("message", successMessage);
            }

            forwardToContentPage(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "An error occurred while fetching the content.");
            forwardToContentPage(request, response);
        }
    }

    private void forwardToContentPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebContext context = new WebContext(request, response, getServletContext(), request.getLocale());
        context.setVariable("folder", request.getAttribute("folder"));
        context.setVariable("subfolders", request.getAttribute("subfolders"));
        context.setVariable("documents", request.getAttribute("documents"));
        context.setVariable("error", request.getAttribute("error"));
        context.setVariable("message", request.getAttribute("message"));
        templateEngine.process("content", context, response.getWriter());
    }
}
