package controllers;

import beans.Document;
import dao.DocumentDAO;
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

@WebServlet("/protected/viewDocument")
public class ViewDocumentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private DocumentDAO documentDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        ThymeleafConfig config = new ThymeleafConfig(getServletContext());
        this.templateEngine = config.getTemplateEngine();
        this.documentDAO = new DocumentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String documentIdStr = request.getParameter("documentId");
        if (documentIdStr == null) {
            response.sendRedirect("error");
            return;
        }

        try {
            int userId = (Integer) session.getAttribute("userId");
            int documentId = Integer.parseInt(documentIdStr);
            Document document = documentDAO.getDocumentById(documentId);

            if (document == null || !documentDAO.isDocumentOwnedByUser(documentId, userId)) {
                response.sendRedirect("error");
                return;
            }

            WebContext context = new WebContext(request, response, getServletContext());
            context.setVariable("document", document);
            templateEngine.process("viewDocument", context, response.getWriter());
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("error");
        }
    }
}
