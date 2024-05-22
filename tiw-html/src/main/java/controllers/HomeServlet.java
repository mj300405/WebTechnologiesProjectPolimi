package controllers;

import beans.Folder;
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

@WebServlet("/protected/home")
public class HomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
    private FolderDAO folderDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        ThymeleafConfig config = new ThymeleafConfig(getServletContext());
        this.templateEngine = config.getTemplateEngine();
        this.folderDAO = new FolderDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        try {
            List<Folder> folders = folderDAO.getAllFoldersByUser(userId);
            String folderTreeHtml = buildFolderTreeHtml(folders, request.getContextPath());

            WebContext context = new WebContext(request, response, getServletContext(), request.getLocale());
            context.setVariable("username", session.getAttribute("username"));
            context.setVariable("folderTreeHtml", folderTreeHtml);
            templateEngine.process("home", context, response.getWriter());
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }

    private String buildFolderTreeHtml(List<Folder> folders, String contextPath) {
        StringBuilder html = new StringBuilder("<ul>");
        for (Folder folder : folders) {
            html.append("<li>");
            html.append("<a href='").append(contextPath).append("/protected/content?folderId=").append(folder.getId()).append("'>")
                .append(folder.getName()).append("</a>");
            if (folder.getSubfolders() != null && !folder.getSubfolders().isEmpty()) {
                html.append(buildFolderTreeHtml(folder.getSubfolders(), contextPath));
            }
            html.append("</li>");
        }
        html.append("</ul>");
        return html.toString();
    }
}
