package com.example.houduan.pojo.dto;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamResultResponseDto {
    private Integer id;
    private Integer examId;
    private LocalDateTime examDate;
    private Integer score;
    private Integer duration;
    private String userAnswers;
}