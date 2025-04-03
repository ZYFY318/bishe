package com.example.houduan.pojo;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_model")
@Data
public class Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String name;
    private String imageUrl;
    private String description;
    
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] glbData;  // 存储GLB文件的二进制数据
    
    private String fileName;  // 存储原始文件名
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructor with parameters
    public Model(Integer id, String name, String imageUrl, String description, byte[] glbData, String fileName) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.glbData = glbData;
        this.fileName = fileName;
    }
    
    // Default constructor
    public Model() {
    }
} 