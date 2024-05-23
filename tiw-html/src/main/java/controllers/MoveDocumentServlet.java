package controllers;

import dao.DocumentDAO;
import dao.FolderDAO;
import utils.ThymeleafConfig;
import utils.SecurityUtils;
import beans.Document;
import beans.Folder;
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
import java.util.List;

@WebServlet("/protected/moveDocument")
public class MoveDocumentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private DocumentDAO documentDAO = new DocumentDAO();
    private FolderDAO folderDAO = new FolderDAO();
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        super.init();
        ThymeleafConfig config = new ThymeleafConfig(getServletContext());
        this.templateEngine = config.getTemplateEngine();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int documentId;
        try {
            documentId = Integer.parseInt(request.getParameter("documentId"));
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid document ID.");
            forwardToMoveDocumentPage(request, response);
            return;
        }

        try {
            int userId = (Integer) session.getAttribute("userId");
            Document document = documentDAO.getDocumentById(documentId);
            if (document == null || document.getUserId() != userId) {
                request.setAttribute("error", "Document not found or you do not have permission to move it.");
                forwardToMoveDocumentPage(request, response);
                return;
            }

            int currentFolderId = document.getFolderId();
            Folder currentFolder = folderDAO.getFolderById(currentFolderId);
            if (currentFolder == null) {
                request.setAttribute("error", "Current folder not found.");
                forwardToMoveDocumentPage(request, response);
                return;
            }

            List<Folder> folders = folderDAO.getAllFoldersByUser(userId);
            String folderOptionsHtml = buildFolderOptionsHtml(folders, currentFolderId, 0);

            request.setAttribute("document", document);
            request.setAttribute("sourceFolderName", currentFolder.getName());
            request.setAttribute("currentFolderId", currentFolderId);
            request.setAttribute("folderOptionsHtml", folderOptionsHtml);
            forwardToMoveDocumentPage(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "An error occurred while fetching the document or folders.");
            forwardToMoveDocumentPage(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SecurityUtils.isCsrfTokenValid(request)) {
            request.setAttribute("error", "Invalid CSRF token.");
            forwardToMoveDocumentPage(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int documentId, destinationFolderId;
        try {
            documentId = Integer.parseInt(request.getParameter("documentId"));
            destinationFolderId = Integer.parseInt(request.getParameter("destinationFolderId"));
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid document or folder ID.");
            forwardToMoveDocumentPage(request, response);
            return;
        }

        try {
            int userId = (Integer) session.getAttribute("userId");
            if (!documentDAO.isDocumentOwnedByUser(documentId, userId) || !folderDAO.isFolderOwnedByUser(destinationFolderId, userId)) {
                request.setAttribute("error", "You do not have permission to move the document or the destination folder is invalid.");
                forwardToMoveDocumentPage(request, response);
                return;
            }

            documentDAO.moveDocumentToFolder(documentId, destinationFolderId);
            request.setAttribute("success", "Document moved successfully.");
            response.sendRedirect(request.getContextPath() + "/protected/content?folderId=" + destinationFolderId);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "An error occurred while moving the document.");
            forwardToMoveDocumentPage(request, response);
        }
    }

    private void forwardToMoveDocumentPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebContext context = new WebContext(request, response, getServletContext(), request.getLocale());
        context.setVariable("document", request.getAttribute("document"));
        context.setVariable("sourceFolderName", request.getAttribute("sourceFolderName"));
        context.setVariable("currentFolderId", request.getAttribute("currentFolderId"));
        context.setVariable("folderOptionsHtml", request.getAttribute("folderOptionsHtml"));
        context.setVariable("error", request.getAttribute("error"));
        context.setVariable("success", request.getAttribute("success"));
        context.setVariable("csrfToken", request.getSession().getAttribute("csrfToken")); // Pass CSRF token to the template
        templateEngine.process("moveDocument", context, response.getWriter());
    }

    private String buildFolderOptionsHtml(List<Folder> folders, int currentFolderId, int level) {
        StringBuilder html = new StringBuilder();
        String indent = "--".repeat(level);

        for (Folder folder : folders) {
            if (folder.getId() == currentFolderId) {
                html.append("<option value='").append(folder.getId()).append("' class='current-folder' disabled>")
                    .append(indent).append(folder.getName()).append("</option>");
            } else {
                html.append("<option value='").append(folder.getId()).append("'>")
                    .append(indent).append(folder.getName()).append("</option>");
            }

            if (folder.getSubfolders() != null && !folder.getSubfolders().isEmpty()) {
                html.append(buildFolderOptionsHtml(folder.getSubfolders(), currentFolderId, level + 1));
            }
        }
        return html.toString();
    }
}
