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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // 外键关联 tb_user 表
    private User user;

    private LocalDateTime examDate; // 考试日期

    private Integer score; // 成绩

    private Integer duration; // 用时（单位：秒）

    private Integer examType; //1.小测试 2.中测试 3.大测试

}
