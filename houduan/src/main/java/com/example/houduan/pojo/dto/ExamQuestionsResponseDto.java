package com.example.houduan.pojo.dto;

import com.example.houduan.pojo.Question;
import lombok.Data;
import java.util.List;

@Data
public class ExamQuestionsResponseDto {
    private List<Question> questions;
    private int total;
} 