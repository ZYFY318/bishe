package com.example.houduan.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ExamResultDto {
    private Integer userId;       // 用户ID替代User对象
    private Integer examId;       // 试卷ID
    private LocalDateTime examDate;
    private Integer score;
    private Integer duration;
    private String userAnswers;   // 用户答案，JSON格式
}
