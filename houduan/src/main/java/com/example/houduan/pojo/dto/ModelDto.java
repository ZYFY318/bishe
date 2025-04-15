package com.example.houduan.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class ModelDto {
    private Integer id;
    
    @NotBlank(message = "模型名称不能为空")
    private String name;
    
    private String coverUrl;
    private String description;
    private String fileName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer creatorId;
    private String creatorName;

    public ModelDto() {
    }

    public ModelDto(Integer id, String name, String coverUrl, String description, 
                   String fileName, LocalDateTime createdAt, LocalDateTime updatedAt,
                   Integer creatorId, String creatorName) {
        this.id = id;
        this.name = name;
        this.coverUrl = coverUrl;
        this.description = description;
        this.fileName = fileName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    @Override
    public String toString() {
        return "ModelDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coverUrl='" + coverUrl + '\'' +
                ", description='" + description + '\'' +
                ", fileName='" + fileName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", creatorId=" + creatorId +
                ", creatorName='" + creatorName + '\'' +
                '}';
    }
} 