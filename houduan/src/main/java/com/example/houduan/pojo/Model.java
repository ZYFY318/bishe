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

    private String coverUrl;
    
    private String description;
    
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] glbData;  // 存储GLB文件的二进制数据
    
    private String fileName;  // 存储原始文件名
    
    // 创建者ID
    private Integer creatorId;
    
    // 创建者名称
    private String creatorName;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
} 