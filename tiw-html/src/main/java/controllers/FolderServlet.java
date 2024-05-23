package controllers;

import beans.Folder;
import dao.FolderDAO;
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
import java.util.List;

@WebServlet("/protected/folders")
public class FolderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private FolderDAO folderDAO = new FolderDAO();
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        super.init();
        ThymeleafConfig config = new ThymeleafConfig(getServletContext());
        this.templateEngine = config.getTemplateEngine();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        try {
            List<Folder> folders = folderDAO.getAllFolders(userId);
            WebContext context = new WebContext(request, response, getServletContext());
            context.setVariable("folders", folders);
            templateEngine.process("home", context, response.getWriter());
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String folderName = request.getParameter("folderName");
        String parentIdStr = request.getParameter("parentId");
        int userId = (Integer) session.getAttribute("userId");

        try {
            Integer parentId = (parentIdStr != null && !parentIdStr.isEmpty()) ? Integer.valueOf(parentIdStr) : null;
            
            if (parentId != null && !folderDAO.isFolderOwnedByUser(parentId, userId)) {
                response.sendRedirect("error");
                return;
            }

            Folder folder = new Folder(folderName, userId, parentId);
            folderDAO.addFolder(folder);
            response.sendRedirect("folders");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("error");
        }
    }
}
