package dao;

import beans.Folder;
import utils.DatabaseUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FolderDAO {

	public List<Folder> getAllFolders(int userId) throws SQLException {
        List<Folder> folders = new ArrayList<>();
        String sql = "SELECT id, name, parent_id, created_at FROM folders WHERE user_id = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    Integer parentId = rs.getObject("parent_id", Integer.class);
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    Folder folder = new Folder(id, name, userId, parentId, createdAt);
                    folders.add(folder);
                }
            }
        }
        return folders;
    }

    public void addFolder(Folder folder) throws SQLException {
        String sql = "INSERT INTO folders (name, user_id, parent_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, folder.getName());
            stmt.setInt(2, folder.getUserId());
            stmt.setObject(3, folder.getParentId());
            stmt.executeUpdate();
        }
    }
    
    public boolean deleteFolderRecursively(int folderId) throws SQLException {
        try (Connection conn = DatabaseUtils.getConnection()) {
            conn.setAutoCommit(false);

            // Recursively delete all subfolders
            List<Integer> subFolderIds = getSubFolderIds(folderId, conn);
            for (Integer subFolderId : subFolderIds) {
                deleteFolderRecursively(subFolderId);
            }

            // Delete all documents in the folder
            deleteDocumentsInFolder(folderId, conn);

            // Delete the folder itself
            boolean isDeleted = deleteFolderById(folderId, conn);

            conn.commit();
            return isDeleted;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Integer> getSubFolderIds(int parentId, Connection conn) throws SQLException {
        String sql = "SELECT id FROM folders WHERE parent_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, parentId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Integer> folderIds = new ArrayList<>();
                while (rs.next()) {
                    folderIds.add(rs.getInt("id"));
                }
                return folderIds;
            }
        }
    }

    private void deleteDocumentsInFolder(int folderId, Connection conn) throws SQLException {
        String sql = "DELETE FROM documents WHERE folder_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, folderId);
            stmt.executeUpdate();
        }
    }

    private boolean deleteFolderById(int folderId, Connection conn) throws SQLException {
        String sql = "DELETE FROM folders WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, folderId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    public boolean isFolderOwnedByUser(int folderId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM folders WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, folderId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
