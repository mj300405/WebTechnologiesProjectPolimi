package dao;

import beans.Folder;
import utils.DatabaseUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FolderDAO {

    public List<Folder> getAllFoldersByUser(int userId) throws SQLException {
        List<Folder> folders = new ArrayList<>();
        String query = "SELECT * FROM folders WHERE user_id = ? AND parent_id IS NULL";

        try (Connection connection = DatabaseUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Folder folder = mapRowToFolder(resultSet);
                    folder.setSubfolders(getSubfolders(folder.getId(), connection));
                    folders.add(folder);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching folders for user: " + userId, e);
        }

        return folders;
    }

    private List<Folder> getSubfolders(int parentId, Connection connection) throws SQLException {
        List<Folder> subfolders = new ArrayList<>();
        String query = "SELECT * FROM folders WHERE parent_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, parentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Folder folder = mapRowToFolder(resultSet);
                    folder.setSubfolders(getSubfolders(folder.getId(), connection));
                    subfolders.add(folder);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching subfolders for parent folder: " + parentId, e);
        }

        return subfolders;
    }

    private Folder mapRowToFolder(ResultSet resultSet) throws SQLException {
        Folder folder = new Folder();
        folder.setId(resultSet.getInt("id"));
        folder.setName(resultSet.getString("name"));
        folder.setUserId(resultSet.getInt("user_id"));
        folder.setParentId(resultSet.getObject("parent_id", Integer.class));
        folder.setCreatedAt(resultSet.getTimestamp("created_at"));
        folder.setSubfolders(new ArrayList<>()); // Initialize subfolders
        return folder;
    }

    public void addFolder(Folder folder) throws SQLException {
        String sql = "INSERT INTO folders (name, created_at, user_id, parent_id) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, folder.getName());
            statement.setTimestamp(2, folder.getCreatedAt());
            statement.setInt(3, folder.getUserId());
            if (folder.getParentId() != null) {
                statement.setInt(4, folder.getParentId());
            } else {
                statement.setNull(4, java.sql.Types.INTEGER);
            }
            statement.executeUpdate();
        }
    }

    public Folder getFolderById(int folderId) throws SQLException {
        String sql = "SELECT id, name, user_id, parent_id, created_at FROM folders WHERE id = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, folderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToFolder(rs);
                }
            }
        }
        return null;
    }

    public List<Folder> getAllFoldersByUser(String username) throws SQLException {
        List<Folder> folders = new ArrayList<>();
        String sql = "SELECT f.id, f.name, f.user_id, f.parent_id, f.created_at FROM folders f " +
                     "JOIN users u ON f.user_id = u.id WHERE u.username = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Folder folder = mapRowToFolder(rs);
                    folder.setSubfolders(getSubfolders(folder.getId(), conn));
                    folders.add(folder);
                }
            }
        }
        return folders;
    }

    public List<Folder> getSubfoldersByFolderId(int folderId) throws SQLException {
        return getSubfolders(folderId, DatabaseUtils.getConnection());
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
    
    public List<Folder> getImmediateSubfolders(int parentId) throws SQLException {
        List<Folder> subfolders = new ArrayList<>();
        String query = "SELECT * FROM folders WHERE parent_id = ?";

        try (Connection connection = DatabaseUtils.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, parentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Folder folder = mapRowToFolder(resultSet);
                    subfolders.add(folder);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching subfolders for parent folder: " + parentId, e);
        }

        return subfolders;
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
