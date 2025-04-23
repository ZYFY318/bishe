package com.example.houduan.pojo;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_exam_statistics")
@Data
public class ExamStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "exam_id", nullable = false, unique = true)
    private Integer examId; // 关联的考试ID
    
    @Column(name = "participant_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer participantCount = 0; // 参与人数
    
    @Column(name = "avg_score", columnDefinition = "DOUBLE DEFAULT 0")
    private Double avgScore = 0.0; // 平均分数
    
    @Column(name = "passing_score", columnDefinition = "INTEGER DEFAULT 60")
    private Integer passingScore = 60; // 及格分数线
    
    @Column(name = "passing_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer passingCount = 0; // 及格人数
    
    @Column(name = "highest_score", columnDefinition = "INTEGER DEFAULT 0")
    private Integer highestScore = 0; // 最高分
    
    @Column(name = "lowest_score", columnDefinition = "INTEGER DEFAULT 0")
    private Integer lowestScore = 0; // 最低分
    
    @Column(name = "total_time", columnDefinition = "BIGINT DEFAULT 0")
    private Long totalTime = 0L; // 总用时(秒)
    
    @Column(name = "avg_time", columnDefinition = "DOUBLE DEFAULT 0")
    private Double avgTime = 0.0; // 平均用时(秒)
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 