package com.example.houduan.pojo;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_question_analysis")
@Data
public class QuestionAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "exam_id", nullable = false)
    private Integer examId; // 关联的考试ID
    
    @Column(name = "question_id", nullable = false)
    private Long questionId; // 题目ID
    
    private String description; // 题目描述摘要
    
    @Column(name = "correct_rate", columnDefinition = "DOUBLE DEFAULT 0")
    private Double correctRate = 0.0; // 正确率
    
    @Column(name = "attempt_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer attemptCount = 0; // 尝试回答的学生数量
    
    @Column(name = "correct_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer correctCount = 0; // 回答正确的学生数量
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 