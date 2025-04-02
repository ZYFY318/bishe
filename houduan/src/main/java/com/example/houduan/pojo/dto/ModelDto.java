package com.example.houduan.pojo.dto;

import jakarta.validation.constraints.NotBlank;

public class ModelDto {
    private Integer id;
    
    @NotBlank(message = "模型名称不能为空")
    private String name;
    
    private String imageUrl;
    private String description;
    private String glbPath;

    public ModelDto(Integer id, String name, String imageUrl, String description, String glbPath) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.glbPath = glbPath;
    }

    public ModelDto() {
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGlbPath() {
        return glbPath;
    }

    public void setGlbPath(String glbPath) {
        this.glbPath = glbPath;
    }

    @Override
    public String toString() {
        return "ModelDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", description='" + description + '\'' +
                ", glbPath='" + glbPath + '\'' +
                '}';
    }
} 