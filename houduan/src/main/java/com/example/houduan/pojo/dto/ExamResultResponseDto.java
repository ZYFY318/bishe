package com.example.houduan.pojo.dto;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamResultResponseDto {
    private Integer id;
    private LocalDateTime examDate;
    private Integer score;
    private Integer duration;
    private Integer examType;
}