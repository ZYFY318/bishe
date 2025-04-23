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
    
    @Column(name = "question_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer questionCount = 0; // 题目数量
    
    @Column(name = "total_score", columnDefinition = "INTEGER DEFAULT 100")
    private Integer totalScore = 100; // 总分值
    
    @Column(name = "passing_score", columnDefinition = "INTEGER DEFAULT 60")
    private Integer passingScore = 60; // 及格分数线
    
    // 创建者用户名，不映射到数据库
    @Transient
    private String creatorName;
    
    // 统计数据，不映射到数据库
    @Transient
    private Integer participantCount; // 参与人数
    
    @Transient
    private Double avgScore; // 平均分数
    
    @Transient
    private Integer passingCount; // 及格人数
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 