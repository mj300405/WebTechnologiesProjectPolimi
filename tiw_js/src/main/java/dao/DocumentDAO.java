package dao;

import beans.Document;
import utils.DatabaseUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {

    public List<Document> getDocumentsByFolder(int folderId) throws SQLException {
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT id, name, user_id, created_at, summary, type FROM documents WHERE folder_id = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, folderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    int userId = rs.getInt("user_id");
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    String summary = rs.getString("summary");
                    String type = rs.getString("type");
                    Document document = new Document(id, name, userId, folderId, createdAt, summary, type);
                    documents.add(document);
                }
            }
        }
        return documents;
    }

    public void addDocument(Document document) throws SQLException {
        String sql = "INSERT INTO documents (name, user_id, folder_id, summary, type) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, document.getName());
            stmt.setInt(2, document.getUserId());
            stmt.setInt(3, document.getFolderId());
            stmt.setString(4, document.getSummary());
            stmt.setString(5, document.getType());
            stmt.executeUpdate();
        }
    }

    public boolean moveDocument(int documentId, int newFolderId) throws SQLException {
        String sql = "UPDATE documents SET folder_id = ? WHERE id = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newFolderId);
            stmt.setInt(2, documentId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean deleteDocument(int documentId) throws SQLException {
        String sql = "DELETE FROM documents WHERE id = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, documentId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    public Document getDocumentById(int documentId) throws SQLException {
        String sql = "SELECT id, name, folder_id, user_id, type, summary, created_at FROM documents WHERE id = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, documentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    int folderId = rs.getInt("folder_id");
                    int userId = rs.getInt("user_id");
                    String type = rs.getString("type");
                    String summary = rs.getString("summary");
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    return new Document(id, name, userId, folderId, createdAt, summary, type);
                }
            }
        }
        return null; // Return null if no document is found
    }
}
