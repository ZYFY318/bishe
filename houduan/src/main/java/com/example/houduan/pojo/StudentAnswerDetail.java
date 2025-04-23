package com.example.houduan.pojo;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_student_answer_detail")
@Data
public class StudentAnswerDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "exam_result_id", nullable = false)
    private Integer examResultId; // 关联的考试结果ID
    
    @Column(name = "question_id", nullable = false)
    private Long questionId; // 题目ID
    
    @Column(name = "question_content", columnDefinition = "TEXT")
    private String questionContent; // 题目内容
    
    @Column(name = "student_answer", columnDefinition = "TEXT")
    private String studentAnswer; // 学生答案
    
    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer; // 正确答案
    
    @Column(name = "is_correct")
    private Boolean isCorrect = false; // 是否正确
    
    @Column(name = "score")
    private Double score = 0.0; // 得分

    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
} 