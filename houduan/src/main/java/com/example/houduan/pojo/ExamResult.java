package com.example.houduan.pojo;
import jakarta.persistence.*;
import lombok.Data;
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
    
    @Column(name = "exam_id")
    private Integer examId; // 关联的试卷ID

    private LocalDateTime examDate; // 考试日期

    private Integer score; // 成绩

    private Integer duration; // 用时（单位：秒）
    
    @Column(columnDefinition = "TEXT")
    private String userAnswers; // 用户答案，JSON格式
}
