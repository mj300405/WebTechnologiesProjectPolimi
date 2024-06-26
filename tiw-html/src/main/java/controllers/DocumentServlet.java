package controllers;

import beans.Document;
import dao.DocumentDAO;
import utils.ThymeleafConfig;
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

@WebServlet("/protected/document")
public class DocumentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private DocumentDAO documentDAO = new DocumentDAO();
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
            forwardToDocumentPage(request, response);
            return;
        }

        try {
            int userId = (Integer) session.getAttribute("userId");
            Document document = documentDAO.getDocumentById(documentId);

            if (document == null || !documentDAO.isDocumentOwnedByUser(documentId, userId)) {
                request.setAttribute("error", "Document not found or you do not have permission to view it.");
            } else {
                request.setAttribute("document", document);
            }
            forwardToDocumentPage(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "An error occurred while fetching the document.");
            forwardToDocumentPage(request, response);
        }
    }

    private void forwardToDocumentPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebContext context = new WebContext(request, response, getServletContext(), request.getLocale());
        context.setVariable("document", request.getAttribute("document"));
        context.setVariable("error", request.getAttribute("error"));
        templateEngine.process("document", context, response.getWriter());
    }
}
