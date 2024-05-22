package beans;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Folder {
    private int id;
    private String name;
    private int userId; // Owner of the folder
    private Integer parentId;
    private Timestamp createdAt;
    private List<Folder> subfolders;

    public Folder() {
        this.subfolders = new ArrayList<>(); // Initialize subfolders
    }

    public Folder(int id, String name, int userId, Integer parentId, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.parentId = parentId;
        this.createdAt = createdAt;
        this.subfolders = new ArrayList<>(); // Initialize subfolders
    }

    public Folder(String name, int userId, Integer parentId) {
        this.name = name;
        this.userId = userId;
        this.parentId = parentId;
        this.subfolders = new ArrayList<>(); // Initialize subfolders
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<Folder> getSubfolders() {
        return subfolders;
    }

    public void setSubfolders(List<Folder> subfolders) {
        this.subfolders = subfolders;
    }
}
