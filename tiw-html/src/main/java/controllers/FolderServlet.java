package controllers;

import beans.Folder;
import dao.FolderDAO;
import dao.UserDAO;
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
    private UserDAO userDAO = new UserDAO();
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        super.init();
        ThymeleafConfig config = new ThymeleafConfig(getServletContext());
        this.templateEngine = config.getTemplateEngine();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("user");

        try {
            int userId = userDAO.getUserIdByUsername(username);
            if (userId == -1) {
                response.sendRedirect("error");
                return;
            }

            List<Folder> folders = folderDAO.getAllFolders(userId);
            WebContext context = new WebContext(request, response, getServletContext());
            context.setVariable("folders", folders);
            templateEngine.process("home", context, response.getWriter());
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("error");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String folderName = request.getParameter("folderName");
        String parentIdStr = request.getParameter("parentId");
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("user");

        try {
            int userId = userDAO.getUserIdByUsername(username);
            if (userId == -1) {
                response.sendRedirect("error");
                return;
            }

            Integer parentId = (parentIdStr != null && !parentIdStr.isEmpty()) ? Integer.valueOf(parentIdStr) : null;
            Folder folder = new Folder(folderName, userId, parentId);
            folderDAO.addFolder(folder);
            response.sendRedirect("folders");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("error");
        }
    }
}
