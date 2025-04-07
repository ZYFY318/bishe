package com.example.houduan.pojo;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_exam")
@Data
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String title;
    
    @Column(name = "cover_url")
    private String coverUrl;
    
    private Integer duration;
    
    @Column(name = "published", columnDefinition = "boolean default false")
    private Boolean published = false;
    
    @Column(name = "creator_id")
    private Integer creatorId;
    
    // 创建者用户名，不映射到数据库
    @Transient
    private String creatorName;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 