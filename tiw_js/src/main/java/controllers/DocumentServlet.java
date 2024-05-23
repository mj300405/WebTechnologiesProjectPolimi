package controllers;

import dao.DocumentDAO;
import dao.FolderDAO;
import beans.Document;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import org.json.JSONObject;

@WebServlet("/api/documents/*")
public class DocumentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = request.getReader().readLine()) != null) {
            sb.append(line);
        }
        String requestData = sb.toString();
        JSONObject json = new JSONObject(requestData);

        int folderId = json.getInt("folderId");
        String documentName = json.getString("name");
        String summary = json.getString("summary");
        String type = json.getString("type");

        // Extract userId from the session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"User not authenticated\"}");
            return;
        }
        int userId = (Integer) session.getAttribute("userId");

        try {
            FolderDAO folderDAO = new FolderDAO();
            // Check if the folder is owned by the user
            if (!folderDAO.isFolderOwnedByUser(folderId, userId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"You do not have access to this folder\"}");
                return;
            }

            Document document = new Document(0, documentName, userId, folderId, new Timestamp(System.currentTimeMillis()), summary, type);
            DocumentDAO documentDAO = new DocumentDAO();
            documentDAO.addDocument(document);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"message\":\"Document added successfully\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Database error occurred\"}");
        }
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in.");
            return;
        }

        Integer userId = (Integer) session.getAttribute("userId");
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Handle getting all documents by folder ID
                String folderIdParam = request.getParameter("folderId");
                if (folderIdParam == null || folderIdParam.trim().isEmpty()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Folder ID is required");
                    return;
                }

                int folderId = Integer.parseInt(folderIdParam);
                FolderDAO folderDAO = new FolderDAO();
                if (!folderDAO.isFolderOwnedByUser(folderId, userId)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have access to this folder");
                    return;
                }

                DocumentDAO documentDAO = new DocumentDAO();
                List<Document> documents = documentDAO.getDocumentsByFolder(folderId);
                String json = convertDocumentListToJson(documents);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(json);
            } else {
                // Handle getting a specific document by ID
                int documentId = Integer.parseInt(pathInfo.substring(1));
                DocumentDAO documentDAO = new DocumentDAO();
                Document document = documentDAO.getDocumentById(documentId);

                if (document != null) {
                    FolderDAO folderDAO = new FolderDAO();
                    if (!folderDAO.isFolderOwnedByUser(document.getFolderId(), userId)) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have access to this document");
                        return;
                    }

                    String json = convertDocumentToJson(document);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Document not found");
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid document ID format");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        }
    }



    


    private String convertDocumentToJson(Document document) {
        return "{"
                + "\"id\":" + document.getId() + ","
                + "\"name\":\"" + escapeJson(document.getName()) + "\","
                + "\"folderId\":" + document.getFolderId() + ","
                + "\"userId\":" + document.getUserId() + ","
                + "\"type\":\"" + escapeJson(document.getType()) + "\","
                + "\"summary\":\"" + escapeJson(document.getSummary()) + "\","
                + "\"createdAt\":\"" + document.getCreatedAt() + "\""
                + "}";
    }

    private String escapeJson(String data) {
        if (data == null) {
            return "";
        }
        return data.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Document ID is required\"}");
            return;
        }

        try {
            int documentId = Integer.parseInt(idParam);
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"User not authenticated\"}");
                return;
            }

            int userId = (Integer) session.getAttribute("userId");
            DocumentDAO documentDAO = new DocumentDAO();
            Document document = documentDAO.getDocumentById(documentId);

            if (document == null || document.getUserId() != userId) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"You do not have access to this document\"}");
                return;
            }

            boolean isDeleted = documentDAO.deleteDocument(documentId);
            if (isDeleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\":\"Document deleted successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Document not found\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid document ID format\"}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error deleting document\"}");
            e.printStackTrace();
        }
    }

    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Document ID is required\"}");
            return;
        }

        String documentIdStr = pathInfo.substring(1);
        int documentId;
        try {
            documentId = Integer.parseInt(documentIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid document ID format\"}");
            return;
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = request.getReader().readLine()) != null) {
            sb.append(line);
        }
        String requestData = sb.toString();
        JSONObject json = new JSONObject(requestData);

        int newFolderId = json.getInt("folderId");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"User not authenticated\"}");
            return;
        }
        int userId = (Integer) session.getAttribute("userId");

        DocumentDAO documentDAO = new DocumentDAO();
        FolderDAO folderDAO = new FolderDAO();
        try {
            Document document = documentDAO.getDocumentById(documentId);
            if (document == null || document.getUserId() != userId) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"You do not have access to this document\"}");
                return;
            }

            if (!folderDAO.isFolderOwnedByUser(newFolderId, userId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"You do not have access to the destination folder\"}");
                return;
            }

            boolean isUpdated = documentDAO.moveDocument(documentId, newFolderId);
            if (isUpdated) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\":\"Document moved successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Document not found\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Database error occurred\"}");
        }
    }


    private String convertDocumentListToJson(List<Document> documents) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            sb.append("{");
            sb.append("\"id\":").append(doc.getId()).append(",");
            sb.append("\"name\":\"").append(escapeJson(doc.getName())).append("\",");
            sb.append("\"userId\":").append(doc.getUserId()).append(",");
            sb.append("\"folderId\":").append(doc.getFolderId()).append(",");
            sb.append("\"createdAt\":\"").append(doc.getCreatedAt()).append("\",");
            sb.append("\"summary\":\"").append(escapeJson(doc.getSummary())).append("\",");
            sb.append("\"type\":\"").append(escapeJson(doc.getType())).append("\"");
            sb.append("}");
            if (i < documents.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

}
