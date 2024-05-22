package beans;

import java.util.List;
import java.util.StringJoiner;
import java.sql.Timestamp;

public class Folder {
    private int id;
    private String name;
    private int userId; // Owner of the folder
    private Integer parentId;
    private Timestamp createdAt;

    public Folder() {
    }

    public Folder(int id, String name, int userId, Integer parentId, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.parentId = parentId;
        this.createdAt = createdAt;
    }

    // Getters and setters
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
    
    public static String toJson(List<Folder> folders) {
        StringJoiner sj = new StringJoiner(",", "[", "]");
        for (Folder folder : folders) {
            StringJoiner folderJson = new StringJoiner(",", "{", "}");
            folderJson.add("\"id\":" + folder.getId());
            folderJson.add("\"name\":\"" + folder.getName().replace("\"", "\\\"") + "\"");
            folderJson.add("\"userId\":" + folder.getUserId());
            folderJson.add("\"parentId\":" + folder.getParentId());
            folderJson.add("\"createdAt\":\"" + folder.getCreatedAt() + "\"");
            sj.add(folderJson.toString());
        }
        return sj.toString();
    }
}
