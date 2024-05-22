package beans;

import java.sql.Timestamp;

public class Document {
    private int id;
    private String name;
    private int folderId;
    private int userId;
    private String type;
    private String summary; // Summary field to replace content
    private Timestamp createdAt;

    public Document(int id, String name, int userId, int folderId, Timestamp createdAt, String summary, String type) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.folderId = folderId;
        this.createdAt = createdAt;
        this.summary = summary;
        this.type = type;
    }

    public Document(String name, int userId, int folderId, String summary, String type) {
        this.name = name;
        this.userId = userId;
        this.folderId = folderId;
        this.summary = summary;
        this.type = type;
    }

    // Getters and setters...

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
