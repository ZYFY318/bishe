package com.example.houduan.pojo;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_exam_result")
@Data
public class ExamResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId; // 用户ID
    
    @Column(name = "exam_id", nullable = false)
    private Integer examId; // 关联的试卷ID

    @Column(name = "exam_date")
    private LocalDateTime examDate; // 考试日期
    
    @Column(name = "start_time")
    private LocalDateTime startTime; // 开始时间
    
    @Column(name = "submit_time")
    private LocalDateTime submitTime; // 提交时间

    private Integer score; // 成绩

    private Integer duration; // 用时（单位：秒）
    
    @Column(columnDefinition = "TEXT")
    private String userAnswers; // 用户答案，JSON格式
    
    @Column(name = "question_count")
    private Integer questionCount = 0; // 试卷题目数量
    
    @Column(name = "answered_count")
    private Integer answeredCount = 0; // 作答题目数量
    
    @Column(name = "correct_count")
    private Integer correctCount = 0; // 正确题目数量
    
    @Column(name = "is_passed")
    private Boolean isPassed = false; // 是否及格
    
    @Transient
    private String studentName; // 学生姓名，不映射到数据库
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
