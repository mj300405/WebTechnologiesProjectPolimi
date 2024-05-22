package controllers;

import dao.DocumentDAO;
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

        Document document = new Document(0, documentName, userId, folderId, new Timestamp(System.currentTimeMillis()), summary, type);
        DocumentDAO documentDAO = new DocumentDAO();
        try {
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
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // Handle getting all documents or by folder ID
            String folderId = request.getParameter("folderId");
            if (folderId == null || folderId.trim().isEmpty()) {
                response.getWriter().write("{\"error\":\"Folder ID is required\"}");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            DocumentDAO documentDAO = new DocumentDAO();
            try {
                List<Document> documents = documentDAO.getDocumentsByFolder(Integer.parseInt(folderId));
                String json = convertDocumentListToJson(documents);
                response.getWriter().write(json);
            } catch (SQLException e) {
                e.printStackTrace();
                response.getWriter().write("{\"error\":\"Database error occurred\"}");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                response.getWriter().write("{\"error\":\"Invalid folder ID format\"}");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            // Handle getting a specific document by ID
            try {
                int documentId = Integer.parseInt(pathInfo.substring(1));
                DocumentDAO documentDAO = new DocumentDAO();
                Document document = documentDAO.getDocumentById(documentId);
                if (document != null) {
                    String json = convertDocumentToJson(document);
                    response.getWriter().write(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.getWriter().write("{\"error\":\"Document not found\"}");
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response.getWriter().write("{\"error\":\"Database error occurred\"}");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                response.getWriter().write("{\"error\":\"Invalid document ID format\"}");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
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
        int documentId = Integer.parseInt(request.getParameter("id"));
        DocumentDAO documentDAO = new DocumentDAO();

        try {
            boolean isDeleted = documentDAO.deleteDocument(documentId);
            if (isDeleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\":\"Document deleted successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Document not found\"}");
            }
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

        DocumentDAO documentDAO = new DocumentDAO();
        try {
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
